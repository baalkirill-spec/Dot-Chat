package com.streamgram.domain.usecase

import com.streamgram.core.model.CallType
import com.streamgram.domain.repository.CallsRepository
import javax.inject.Inject

class ObserveActiveCallUseCase @Inject constructor(
    private val repository: CallsRepository,
) {
    operator fun invoke() = repository.observeActiveCall()
}

class StartCallUseCase @Inject constructor(
    private val repository: CallsRepository,
) {
    suspend operator fun invoke(targetId: String, type: CallType) = repository.startCall(targetId, type)
}

class JoinCallUseCase @Inject constructor(
    private val repository: CallsRepository,
) {
    suspend operator fun invoke(roomId: String) = repository.joinRoom(roomId)
}

class LeaveCurrentCallUseCase @Inject constructor(
    private val repository: CallsRepository,
) {
    suspend operator fun invoke() = repository.leaveCurrentCall()
}
