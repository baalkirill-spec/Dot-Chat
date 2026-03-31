package com.streamgram.feature.privacy

import com.streamgram.core.model.PrivacySettings

data class PrivacyUiState(
    val settings: PrivacySettings? = null,
)
