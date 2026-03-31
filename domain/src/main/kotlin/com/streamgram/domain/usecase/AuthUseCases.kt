package com.streamgram.domain.usecase

import com.streamgram.core.model.AttachmentValidationResult
import com.streamgram.core.model.AuthState
import com.streamgram.core.model.LocalAttachment
import com.streamgram.core.model.MAX_UPLOAD_FILE_SIZE_BYTES
import com.streamgram.core.model.ProfileSetupDraft
import com.streamgram.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveAuthorizationStateUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> = repository.observeAuthorizationState()
}

class MarkWelcomeSeenUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.markWelcomeSeen()
}

class SubmitPhoneNumberUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: String, emailAddress: String) =
        repository.submitPhoneNumber(phoneNumber, emailAddress)
}

class SubmitCodeUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(code: String) = repository.submitCode(code)
}

class ResendCodeUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.resendCode()
}

class SubmitPasswordUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(password: String) = repository.submitPassword(password)
}

class SubmitProfileSetupUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(draft: ProfileSetupDraft) = repository.submitProfileSetup(draft)
}

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.logout()
}

class RequestAccountDeletionUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.requestAccountDeletion()
}

class ValidateAttachmentUseCase @Inject constructor() {
    operator fun invoke(attachment: LocalAttachment): AttachmentValidationResult {
        return if (attachment.sizeBytes > MAX_UPLOAD_FILE_SIZE_BYTES) {
            AttachmentValidationResult.FileTooLarge(
                maxBytes = MAX_UPLOAD_FILE_SIZE_BYTES,
                actualBytes = attachment.sizeBytes,
            )
        } else {
            AttachmentValidationResult.Valid
        }
    }
}
