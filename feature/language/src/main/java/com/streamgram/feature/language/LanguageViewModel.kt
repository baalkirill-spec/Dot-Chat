package com.streamgram.feature.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.i18n.applyAppLanguage
import com.streamgram.domain.usecase.ObserveSettingsUseCase
import com.streamgram.domain.usecase.SetAppLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LanguageViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
) : ViewModel() {
    val uiState = observeSettingsUseCase()
        .map { settings -> LanguageUiState(selectedLanguageTag = settings.selectedLanguageTag) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LanguageUiState(),
        )

    fun selectLanguage(languageTag: String) {
        viewModelScope.launch {
            setAppLanguageUseCase(languageTag)
            applyAppLanguage(languageTag)
        }
    }
}
