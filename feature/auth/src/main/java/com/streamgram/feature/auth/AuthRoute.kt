package com.streamgram.feature.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamSecondaryButton
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamStatPill
import com.streamgram.core.designsystem.component.StreamTextField
import com.streamgram.core.designsystem.theme.LocalDotChatMotionPreferences
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R
import com.streamgram.core.model.AuthState
import com.streamgram.core.ui.StreamAvatar

private const val AUTH_CODE_LENGTH = 8

@Composable
fun AuthRoute(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val reducedMotion = LocalDotChatMotionPreferences.current.reducedMotionEnabled
    val scrollState = rememberScrollState()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        viewModel.onAvatarUriChanged(uri?.toString())
    }

    LaunchedEffect(state.authorizationState) {
        if (state.authorizationState is AuthState.Authenticated) {
            onAuthenticated()
        }
    }

    BackHandler(enabled = state.activeStep != AuthStep.EMAIL || state.profileSetupStep != ProfileSetupStep.NAME) {
        viewModel.back()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = StreamTheme.spacing.lg, vertical = StreamTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg),
    ) {
        AuthHeader(
            state = state,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg),
            ) {
                AnimatedContent(
                    targetState = state.activeStep to state.profileSetupStep,
                    transitionSpec = {
                        if (reducedMotion) {
                            fadeIn() togetherWith fadeOut()
                        } else {
                            slideInHorizontally { it / 6 } + fadeIn() togetherWith
                                slideOutHorizontally { -it / 6 } + fadeOut()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = "auth_flow",
                ) { (step, profileStep) ->
                    when (step) {
                        AuthStep.EMAIL -> EmailCard(
                            state = state,
                            onEmailChanged = viewModel::onEmailChanged,
                            onContinue = viewModel::continueFromEmail,
                        )

                        AuthStep.PHONE -> PhoneCard(
                            state = state,
                            onPhoneChanged = viewModel::onPhoneChanged,
                            onContinue = viewModel::submitPhone,
                            onBack = viewModel::back,
                        )

                        AuthStep.CODE -> CodeCard(
                            state = state,
                            destination = state.verificationDestination,
                            onCodeChanged = viewModel::onCodeChanged,
                            onContinue = viewModel::submitCode,
                            onResend = viewModel::resendCode,
                            onBack = viewModel::back,
                        )

                        AuthStep.PASSWORD -> PasswordCard(
                            state = state,
                            onPasswordChanged = viewModel::onPasswordChanged,
                            onContinue = viewModel::submitPassword,
                            onBack = viewModel::back,
                        )

                        AuthStep.PROFILE_SETUP -> ProfileSetupCard(
                            state = state,
                            profileStep = profileStep,
                            onFirstNameChanged = viewModel::onFirstNameChanged,
                            onLastNameChanged = viewModel::onLastNameChanged,
                            onUsernameChanged = viewModel::onUsernameChanged,
                            onBioChanged = viewModel::onBioChanged,
                            onPickPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            onContinue = viewModel::continueProfileSetup,
                            onSkip = viewModel::skipOptionalProfileStep,
                            onBack = viewModel::back,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(StreamTheme.spacing.xl))
            }
        }
    }
}

@Composable
private fun AuthHeader(
    state: AuthUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StreamStatPill(label = stringResource(R.string.auth_runtime_badge))
            StreamStatPill(label = stringResource(progressLabelRes(state)))
        }
        LinearProgressIndicator(
            progress = { progressFor(state) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100)),
        )
        Text(
            text = stringResource(R.string.auth_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(descriptionRes(state)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PhoneCard(
    state: AuthUiState,
    onPhoneChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val phoneValid = state.phoneInput.filter(Char::isDigit).length >= 10

    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.auth_phone_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.auth_phone_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StreamTextField(
                value = state.phoneInput,
                onValueChange = onPhoneChanged,
                label = stringResource(R.string.auth_phone_label),
                placeholder = stringResource(R.string.auth_phone_placeholder),
                isError = state.errorKey == "invalid_phone",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
            )
            ErrorText(errorKey = state.errorKey)
            StreamPrimaryButton(
                text = stringResource(R.string.auth_phone_continue),
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneValid,
                isLoading = state.isSubmitting,
                onClick = onContinue,
            )
            StreamSecondaryButton(
                text = stringResource(R.string.onboarding_back),
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun EmailCard(
    state: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val emailValid = state.emailInput.matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE))

    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.auth_email_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.auth_email_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StreamTextField(
                value = state.emailInput,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.auth_email_label),
                placeholder = stringResource(R.string.auth_email_placeholder),
                isError = state.errorKey == "invalid_email",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
            )
            ErrorText(errorKey = state.errorKey)
            StreamPrimaryButton(
                text = stringResource(R.string.auth_email_continue),
                modifier = Modifier.fillMaxWidth(),
                enabled = emailValid,
                onClick = onContinue,
            )
        }
    }
}

@Composable
private fun CodeCard(
    state: AuthUiState,
    destination: String,
    onCodeChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
) {
    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.auth_code_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.auth_code_body, destination),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StreamTextField(
                value = state.codeInput,
                onValueChange = onCodeChanged,
                label = stringResource(R.string.auth_code_label),
                placeholder = stringResource(R.string.auth_code_placeholder),
                isError = state.errorKey == "invalid_code",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
            )
            ErrorText(errorKey = state.errorKey)
            StreamPrimaryButton(
                text = stringResource(R.string.auth_code_continue),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.codeInput.length == AUTH_CODE_LENGTH,
                isLoading = state.isSubmitting,
                onClick = onContinue,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm)) {
                StreamSecondaryButton(
                    text = stringResource(R.string.onboarding_back),
                    modifier = Modifier.weight(1f),
                    onClick = onBack,
                )
                StreamSecondaryButton(
                    text = if (state.canResend) {
                        stringResource(R.string.auth_resend_code)
                    } else {
                        stringResource(R.string.auth_resend_code_wait, state.resendRemainingSeconds)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = state.canResend,
                    onClick = onResend,
                )
            }
        }
    }
}

@Composable
private fun PasswordCard(
    state: AuthUiState,
    onPasswordChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.auth_password_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.auth_password_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StreamTextField(
                value = state.passwordInput,
                onValueChange = onPasswordChanged,
                label = stringResource(R.string.auth_password_label),
                isError = state.errorKey == "invalid_password",
            )
            ErrorText(errorKey = state.errorKey)
            StreamPrimaryButton(
                text = stringResource(R.string.auth_password_continue),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.passwordInput.length >= 4,
                isLoading = state.isSubmitting,
                onClick = onContinue,
            )
            StreamSecondaryButton(
                text = stringResource(R.string.onboarding_back),
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun ProfileSetupCard(
    state: AuthUiState,
    profileStep: ProfileSetupStep,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.auth_profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(profileDescriptionRes(profileStep)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when (profileStep) {
                ProfileSetupStep.NAME -> {
                    StreamTextField(
                        value = state.profileDraft.firstName,
                        onValueChange = onFirstNameChanged,
                        label = stringResource(R.string.auth_profile_first_name),
                        isError = state.errorKey == "invalid_profile_name",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    StreamTextField(
                        value = state.profileDraft.lastName,
                        onValueChange = onLastNameChanged,
                        label = stringResource(R.string.auth_profile_last_name),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                    )
                }

                ProfileSetupStep.USERNAME -> {
                    StreamTextField(
                        value = state.profileDraft.username,
                        onValueChange = onUsernameChanged,
                        label = stringResource(R.string.auth_profile_username),
                        placeholder = stringResource(R.string.auth_profile_username_placeholder),
                        isError = state.errorKey == "invalid_profile_username",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done,
                        ),
                    )
                }

                ProfileSetupStep.PHOTO -> {
                    StreamAvatar(
                        imageUrl = state.profileDraft.avatarUri,
                        fallbackLabel = state.profileDraft.firstName.ifBlank { stringResource(R.string.app_name) },
                        size = 88.dp,
                    )
                    StreamSecondaryButton(
                        text = stringResource(R.string.auth_profile_photo),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onPickPhoto,
                    )
                }

                ProfileSetupStep.BIO -> {
                    StreamTextField(
                        value = state.profileDraft.bio,
                        onValueChange = onBioChanged,
                        label = stringResource(R.string.auth_profile_bio),
                        placeholder = stringResource(R.string.auth_profile_bio_placeholder),
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default,
                        ),
                        keyboardActions = KeyboardActions.Default,
                    )
                }
            }

            ErrorText(errorKey = state.errorKey)

            StreamPrimaryButton(
                text = stringResource(primaryProfileActionRes(profileStep)),
                modifier = Modifier.fillMaxWidth(),
                enabled = isProfileStepValid(state),
                isLoading = state.isSubmitting,
                onClick = onContinue,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm)) {
                StreamSecondaryButton(
                    text = stringResource(R.string.onboarding_back),
                    modifier = Modifier.weight(1f),
                    onClick = onBack,
                )
                if (profileStep == ProfileSetupStep.PHOTO || profileStep == ProfileSetupStep.BIO) {
                    StreamSecondaryButton(
                        text = stringResource(R.string.auth_profile_skip),
                        modifier = Modifier.weight(1f),
                        onClick = onSkip,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorText(
    errorKey: String?,
) {
    AnimatedVisibility(visible = errorKey != null) {
        Text(
            text = when {
                errorKey == "invalid_phone" -> stringResource(R.string.auth_error_invalid_phone)
                errorKey == "invalid_email" -> stringResource(R.string.auth_error_invalid_email)
                errorKey == "invalid_code" -> stringResource(R.string.auth_error_invalid_code)
                errorKey == "invalid_password" -> stringResource(R.string.auth_error_invalid_password)
                errorKey == "invalid_profile" -> stringResource(R.string.auth_error_invalid_profile)
                errorKey == "invalid_profile_name" -> stringResource(R.string.auth_error_invalid_profile_name)
                errorKey == "invalid_profile_username" -> stringResource(R.string.auth_error_invalid_profile_username)
                errorKey == null -> ""
                errorKey.contains("429") || errorKey.contains("too many", ignoreCase = true) ->
                    stringResource(R.string.auth_error_rate_limited)
                errorKey.contains("403") ||
                    errorKey.contains("expired", ignoreCase = true) ||
                    errorKey.contains("invalid otp", ignoreCase = true) ||
                    errorKey.contains("token", ignoreCase = true) ->
                    stringResource(R.string.auth_error_code_stale)
                else -> stringResource(R.string.auth_error_generic)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

private fun progressFor(state: AuthUiState): Float {
    return when (state.activeStep) {
        AuthStep.EMAIL -> 0.12f
        AuthStep.PHONE -> 0.28f
        AuthStep.CODE -> 0.48f
        AuthStep.PASSWORD -> 0.62f
        AuthStep.PROFILE_SETUP -> when (state.profileSetupStep) {
            ProfileSetupStep.NAME -> 0.68f
            ProfileSetupStep.USERNAME -> 0.8f
            ProfileSetupStep.PHOTO -> 0.9f
            ProfileSetupStep.BIO -> 1f
        }
    }
}

private fun progressLabelRes(state: AuthUiState): Int {
    return when (state.activeStep) {
        AuthStep.EMAIL -> R.string.auth_progress_email
        AuthStep.PHONE -> R.string.auth_progress_phone
        AuthStep.CODE -> R.string.auth_progress_code
        AuthStep.PASSWORD -> R.string.auth_progress_password
        AuthStep.PROFILE_SETUP -> when (state.profileSetupStep) {
            ProfileSetupStep.NAME -> R.string.auth_progress_profile_name
            ProfileSetupStep.USERNAME -> R.string.auth_progress_profile_username
            ProfileSetupStep.PHOTO -> R.string.auth_progress_profile_photo
            ProfileSetupStep.BIO -> R.string.auth_progress_profile_bio
        }
    }
}

private fun descriptionRes(state: AuthUiState): Int {
    return when (state.activeStep) {
        AuthStep.EMAIL -> R.string.auth_subtitle_email
        AuthStep.PHONE -> R.string.auth_subtitle_phone
        AuthStep.CODE -> R.string.auth_subtitle_code
        AuthStep.PASSWORD -> R.string.auth_subtitle_password
        AuthStep.PROFILE_SETUP -> R.string.auth_subtitle_profile
    }
}

private fun primaryProfileActionRes(step: ProfileSetupStep): Int {
    return when (step) {
        ProfileSetupStep.NAME -> R.string.onboarding_continue
        ProfileSetupStep.USERNAME -> R.string.onboarding_continue
        ProfileSetupStep.PHOTO -> R.string.onboarding_continue
        ProfileSetupStep.BIO -> R.string.auth_profile_finish
    }
}

private fun profileDescriptionRes(step: ProfileSetupStep): Int {
    return when (step) {
        ProfileSetupStep.NAME -> R.string.auth_profile_body_name
        ProfileSetupStep.USERNAME -> R.string.auth_profile_body_username
        ProfileSetupStep.PHOTO -> R.string.auth_profile_body_photo
        ProfileSetupStep.BIO -> R.string.auth_profile_body_bio
    }
}

private fun isProfileStepValid(state: AuthUiState): Boolean {
    return when (state.profileSetupStep) {
        ProfileSetupStep.NAME -> state.profileDraft.firstName.isNotBlank()
        ProfileSetupStep.USERNAME -> state.profileDraft.username.matches(Regex("[a-z0-9_]{4,32}"))
        ProfileSetupStep.PHOTO -> true
        ProfileSetupStep.BIO -> true
    }
}
