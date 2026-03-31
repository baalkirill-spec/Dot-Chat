package com.streamgram.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.AuthState
import com.streamgram.core.model.ProfileSetupDraft
import com.streamgram.domain.usecase.ObserveAuthorizationStateUseCase
import com.streamgram.domain.usecase.ResendCodeUseCase
import com.streamgram.domain.usecase.SubmitCodeUseCase
import com.streamgram.domain.usecase.SubmitPasswordUseCase
import com.streamgram.domain.usecase.SubmitPhoneNumberUseCase
import com.streamgram.domain.usecase.SubmitProfileSetupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

private const val AUTH_CODE_LENGTH = 8

@HiltViewModel
class AuthViewModel @Inject constructor(
    observeAuthorizationStateUseCase: ObserveAuthorizationStateUseCase,
    private val submitPhoneNumberUseCase: SubmitPhoneNumberUseCase,
    private val submitCodeUseCase: SubmitCodeUseCase,
    private val resendCodeUseCase: ResendCodeUseCase,
    private val submitPasswordUseCase: SubmitPasswordUseCase,
    private val submitProfileSetupUseCase: SubmitProfileSetupUseCase,
) : ViewModel() {
    private val inputState = MutableStateFlow(AuthUiState())
    private val overrideStep = MutableStateFlow<AuthStep?>(null)
    private val countdown = MutableStateFlow(0)
    private var countdownJob: Job? = null
    private var authActionJob: Job? = null
    private var previousAuthorizationState: AuthState = AuthState.Initial

    val uiState = combine(
        inputState,
        observeAuthorizationStateUseCase(),
        overrideStep,
        countdown,
    ) { input, authorizationState, forcedStep, remainingSeconds ->
        val resolvedStep = resolveStep(
            authorizationState = authorizationState,
            currentStep = input.activeStep,
            forcedStep = forcedStep,
            awaitingVerification = input.awaitingVerification,
        )
        val canResendNow = when (authorizationState) {
            is AuthState.WaitCode -> authorizationState.canResend || remainingSeconds == 0
            else -> false
        }
        input.copy(
            authorizationState = authorizationState,
            activeStep = resolvedStep,
            isSubmitting = authorizationState is AuthState.Loading,
            errorKey = (authorizationState as? AuthState.Error)?.message ?: input.errorKey,
            resendRemainingSeconds = remainingSeconds,
            canResend = canResendNow,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthUiState(),
    )

    init {
        viewModelScope.launch {
            observeAuthorizationStateUseCase().collect { authorizationState ->
                when (authorizationState) {
                    is AuthState.WaitCode -> {
                        startCountdown(authorizationState.timeoutSeconds)
                        inputState.update {
                            it.copy(
                                activeStep = AuthStep.CODE,
                                verificationDestination = authorizationState.emailAddress,
                                awaitingVerification = true,
                                errorKey = null,
                            )
                        }
                    }
                    is AuthState.WaitPassword -> {
                        stopCountdown()
                        inputState.update {
                            it.copy(
                                activeStep = AuthStep.PASSWORD,
                                errorKey = null,
                            )
                        }
                    }
                    is AuthState.WaitPhone -> {
                        stopCountdown()
                        inputState.update {
                            it.copy(
                                activeStep = when (it.activeStep) {
                                    AuthStep.PASSWORD,
                                    AuthStep.PROFILE_SETUP -> AuthStep.EMAIL
                                    AuthStep.CODE -> if (it.awaitingVerification) AuthStep.CODE else AuthStep.EMAIL
                                    else -> it.activeStep
                                },
                            )
                        }
                    }
                    is AuthState.Authenticated -> {
                        stopCountdown()
                    }
                    is AuthState.Error -> {
                        stopCountdown()
                    }
                    else -> stopCountdown()
                }

                if (authorizationState !is AuthState.WaitCode && authorizationState !is AuthState.WaitPassword) {
                    overrideStep.value = null
                }

                if (authorizationState is AuthState.WaitProfileSetup) {
                    val enteringProfileSetup = previousAuthorizationState !is AuthState.WaitProfileSetup
                    if (enteringProfileSetup) {
                        inputState.update {
                            it.copy(
                                activeStep = AuthStep.PROFILE_SETUP,
                                profileDraft = authorizationState.draft,
                                profileSetupStep = ProfileSetupStep.NAME,
                                awaitingVerification = false,
                                errorKey = null,
                            )
                        }
                    }
                }

                previousAuthorizationState = authorizationState
            }
        }
    }

    fun onPhoneChanged(value: String) {
        inputState.update { it.copy(phoneInput = value, errorKey = null) }
    }

    fun onEmailChanged(value: String) {
        inputState.update { it.copy(emailInput = value.trim(), errorKey = null) }
    }

    fun onCodeChanged(value: String) {
        inputState.update { it.copy(codeInput = value.filter(Char::isDigit).take(AUTH_CODE_LENGTH), errorKey = null) }
    }

    fun onPasswordChanged(value: String) {
        inputState.update { it.copy(passwordInput = value, errorKey = null) }
    }

    fun onFirstNameChanged(value: String) {
        inputState.update { it.copy(profileDraft = it.profileDraft.copy(firstName = value), errorKey = null) }
    }

    fun onLastNameChanged(value: String) {
        inputState.update { it.copy(profileDraft = it.profileDraft.copy(lastName = value), errorKey = null) }
    }

    fun onUsernameChanged(value: String) {
        inputState.update {
            it.copy(
                profileDraft = it.profileDraft.copy(username = value.lowercase().replace(" ", "")),
                errorKey = null,
            )
        }
    }

    fun onBioChanged(value: String) {
        inputState.update { it.copy(profileDraft = it.profileDraft.copy(bio = value), errorKey = null) }
    }

    fun onAvatarUriChanged(value: String?) {
        inputState.update { it.copy(profileDraft = it.profileDraft.copy(avatarUri = value), errorKey = null) }
    }

    fun continueFromEmail() {
        val normalizedEmail = normalizedEmailAddress()
        if (!normalizedEmail.matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE))) {
            inputState.update { it.copy(errorKey = "invalid_email") }
            return
        }
        inputState.update {
            it.copy(
                emailInput = normalizedEmail,
                verificationDestination = normalizedEmail,
                activeStep = AuthStep.PHONE,
                awaitingVerification = false,
                errorKey = null,
            )
        }
    }

    fun submitPhone() {
        if (authActionJob?.isActive == true || uiState.value.isSubmitting) return
        val normalizedPhone = normalizedPhoneNumber()
        val normalizedEmail = normalizedEmailAddress()
        if (normalizedPhone.filter(Char::isDigit).length < 10) {
            inputState.update { it.copy(errorKey = "invalid_phone") }
            return
        }
        if (!normalizedEmail.matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE))) {
            inputState.update { it.copy(errorKey = "invalid_email") }
            return
        }
        inputState.update {
            it.copy(
                activeStep = AuthStep.CODE,
                verificationDestination = normalizedEmail,
                awaitingVerification = true,
                codeInput = "",
                errorKey = null,
            )
        }
        startCountdown(60)
        authActionJob = viewModelScope.launch {
            overrideStep.value = null
            submitPhoneNumberUseCase(
                phoneNumber = normalizedPhone,
                emailAddress = normalizedEmail,
            )
        }
    }

    fun submitCode() {
        if (authActionJob?.isActive == true || uiState.value.isSubmitting) return
        authActionJob = viewModelScope.launch {
            overrideStep.value = null
            submitCodeUseCase(inputState.value.codeInput)
        }
    }

    fun resendCode() {
        if (!uiState.value.canResend || authActionJob?.isActive == true || uiState.value.isSubmitting) return
        inputState.update {
            it.copy(
                activeStep = AuthStep.CODE,
                awaitingVerification = true,
                errorKey = null,
            )
        }
        startCountdown(60)
        authActionJob = viewModelScope.launch {
            resendCodeUseCase()
        }
    }

    fun submitPassword() {
        if (authActionJob?.isActive == true || uiState.value.isSubmitting) return
        authActionJob = viewModelScope.launch {
            overrideStep.value = null
            submitPasswordUseCase(inputState.value.passwordInput)
        }
    }

    fun continueProfileSetup() {
        when (uiState.value.profileSetupStep) {
            ProfileSetupStep.NAME -> {
                if (inputState.value.profileDraft.firstName.isBlank()) {
                    inputState.update { it.copy(errorKey = "invalid_profile_name") }
                    return
                }
                inputState.update { it.copy(profileSetupStep = ProfileSetupStep.USERNAME, errorKey = null) }
            }

            ProfileSetupStep.USERNAME -> {
                if (!isUsernameValid(inputState.value.profileDraft.username)) {
                    inputState.update { it.copy(errorKey = "invalid_profile_username") }
                    return
                }
                inputState.update { it.copy(profileSetupStep = ProfileSetupStep.PHOTO, errorKey = null) }
            }

            ProfileSetupStep.PHOTO -> {
                inputState.update { it.copy(profileSetupStep = ProfileSetupStep.BIO, errorKey = null) }
            }

            ProfileSetupStep.BIO -> submitProfileSetup()
        }
    }

    fun skipOptionalProfileStep() {
        when (uiState.value.profileSetupStep) {
            ProfileSetupStep.PHOTO -> inputState.update { it.copy(profileSetupStep = ProfileSetupStep.BIO, errorKey = null) }
            ProfileSetupStep.BIO -> submitProfileSetup()
            else -> Unit
        }
    }

    fun back() {
        when (uiState.value.activeStep) {
            AuthStep.EMAIL -> Unit
            AuthStep.PHONE -> {
                overrideStep.value = AuthStep.EMAIL
                inputState.update { it.copy(awaitingVerification = false) }
            }
            AuthStep.CODE -> {
                overrideStep.value = AuthStep.PHONE
                inputState.update {
                    it.copy(
                        awaitingVerification = false,
                        codeInput = "",
                        errorKey = null,
                    )
                }
                stopCountdown()
            }
            AuthStep.PASSWORD -> overrideStep.value = AuthStep.CODE
            AuthStep.PROFILE_SETUP -> backInProfileSetup()
        }
    }

    private fun submitProfileSetup() {
        if (authActionJob?.isActive == true || uiState.value.isSubmitting) return
        authActionJob = viewModelScope.launch {
            submitProfileSetupUseCase(inputState.value.profileDraft)
        }
    }

    private fun backInProfileSetup() {
        inputState.update {
            it.copy(
                profileSetupStep = when (it.profileSetupStep) {
                    ProfileSetupStep.NAME -> ProfileSetupStep.NAME
                    ProfileSetupStep.USERNAME -> ProfileSetupStep.NAME
                    ProfileSetupStep.PHOTO -> ProfileSetupStep.USERNAME
                    ProfileSetupStep.BIO -> ProfileSetupStep.PHOTO
                },
                errorKey = null,
            )
        }
    }

    private fun resolveStep(
        authorizationState: AuthState,
        currentStep: AuthStep,
        forcedStep: AuthStep?,
        awaitingVerification: Boolean,
    ): AuthStep {
        forcedStep?.let { step ->
            when (authorizationState) {
                is AuthState.WaitCode -> if (step == AuthStep.EMAIL || step == AuthStep.PHONE || step == AuthStep.CODE) return step
                is AuthState.WaitPassword -> if (step == AuthStep.CODE || step == AuthStep.PASSWORD) return step
                else -> Unit
            }
        }

        return when (authorizationState) {
            AuthState.Initial,
            is AuthState.WaitPhone -> if (awaitingVerification && currentStep == AuthStep.CODE) AuthStep.CODE else currentStep
            is AuthState.Error -> if (awaitingVerification && currentStep == AuthStep.CODE) AuthStep.CODE else currentStep
            AuthState.Loading -> currentStep
            is AuthState.WaitCode -> AuthStep.CODE
            is AuthState.WaitPassword -> AuthStep.PASSWORD
            is AuthState.WaitProfileSetup -> AuthStep.PROFILE_SETUP
            is AuthState.Authenticated -> AuthStep.EMAIL
        }
    }

    private fun normalizedPhoneNumber(): String {
        val local = inputState.value.phoneInput.trim()
        if (local.startsWith("+")) return local
        val digits = local.filter(Char::isDigit)
        val prefix = countryCallingCodeFor(Locale.getDefault().country)
        return "$prefix$digits"
    }

    private fun normalizedEmailAddress(): String {
        return inputState.value.emailInput.trim().lowercase(Locale.ROOT)
    }

    private fun isUsernameValid(username: String): Boolean {
        return username.matches(Regex("[a-z0-9_]{4,32}"))
    }

    private fun countryCallingCodeFor(countryIso: String): String {
        return when (countryIso.uppercase(Locale.ROOT)) {
            "RU", "KZ" -> "+7"
            "UA" -> "+380"
            "BY" -> "+375"
            "DE" -> "+49"
            "FR" -> "+33"
            "ES" -> "+34"
            "IT" -> "+39"
            "TR" -> "+90"
            "CN" -> "+86"
            "JP" -> "+81"
            "IN" -> "+91"
            "ID" -> "+62"
            "AE", "SA" -> "+971"
            "BR" -> "+55"
            "PT" -> "+351"
            else -> "+1"
        }
    }

    private fun startCountdown(timeoutSeconds: Int) {
        countdownJob?.cancel()
        countdown.value = timeoutSeconds
        countdownJob = viewModelScope.launch {
            while (countdown.value > 0) {
                delay(1_000)
                countdown.value -= 1
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        countdown.value = 0
    }
}
