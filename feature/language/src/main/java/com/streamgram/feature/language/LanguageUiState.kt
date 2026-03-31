package com.streamgram.feature.language

import com.streamgram.core.i18n.AppLanguage

data class LanguageUiState(
    val selectedLanguageTag: String = AppLanguage.SYSTEM.tag,
    val languages: List<AppLanguage> = AppLanguage.supported,
)
