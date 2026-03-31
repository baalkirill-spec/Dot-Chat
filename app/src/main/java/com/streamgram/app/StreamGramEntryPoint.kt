package com.streamgram.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import com.streamgram.app.navigation.StreamGramApp
import com.streamgram.core.designsystem.theme.DotChatTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun StreamGramEntryPoint() {
    val viewModel: RootViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    DotChatTheme(
        reducedMotion = state.reducedMotionEnabled,
    ) {
        StreamGramApp(rootState = state)
    }
}
