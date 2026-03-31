package com.streamgram.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamSearchBar
import com.streamgram.core.designsystem.component.StreamSecondaryButton
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamStatPill
import com.streamgram.core.designsystem.theme.LocalDotChatMotionPreferences
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.AppLanguage
import com.streamgram.core.i18n.R
import androidx.compose.ui.res.stringResource

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val reducedMotion = LocalDotChatMotionPreferences.current.reducedMotionEnabled

    BackHandler(enabled = state.step != OnboardingStep.LANGUAGE) {
        viewModel.back()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(horizontal = StreamTheme.spacing.lg, vertical = StreamTheme.spacing.xl),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StreamStatPill(label = stringResource(stepBadge(state.step)))
                StreamStatPill(label = stringResource(progressBadge(state.step)))
            }
            LinearProgressIndicator(
                progress = { progress(state.step) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.onboarding_intro_caption),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                if (reducedMotion) {
                    fadeIn() togetherWith fadeOut()
                } else {
                    slideInHorizontally { it / 6 } + fadeIn() togetherWith
                        slideOutHorizontally { -it / 6 } + fadeOut()
                }
            },
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            label = "onboarding_step",
        ) { step ->
            when (step) {
                OnboardingStep.LANGUAGE -> LanguageStepCard(
                    selectedLanguage = state.selectedLanguage,
                    onLanguageSelected = viewModel::onLanguageSelected,
                    onContinue = viewModel::continueFromLanguage,
                )

                OnboardingStep.WELCOME -> WelcomeStepCard(
                    onContinue = viewModel::continueFromWelcome,
                    onBack = viewModel::back,
                )

                OnboardingStep.LEGAL -> LegalStepCard(
                    accepted = state.legalAccepted,
                    submitting = state.isSubmitting,
                    onToggleAccepted = viewModel::setLegalAccepted,
                    onContinue = viewModel::completeOnboarding,
                    onBack = viewModel::back,
                )
            }
        }
    }
}

@Composable
private fun LanguageStepCard(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit,
) {
    val query = remember { mutableStateOf("") }
    val filteredLanguages = AppLanguage.supported.filter { language ->
        val search = query.value.trim()
        search.isBlank() || language.nativeName.contains(search, ignoreCase = true) || language.tag.contains(search, ignoreCase = true)
    }

    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.onboarding_language_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.onboarding_language_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StreamSearchBar(
                value = query.value,
                onValueChange = { query.value = it },
                placeholder = stringResource(R.string.onboarding_language_search),
            )
            LazyColumn(
                modifier = Modifier.height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredLanguages, key = AppLanguage::tag) { language ->
                    StreamSectionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) },
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(language.nativeName, style = MaterialTheme.typography.titleMedium)
                                Text(language.tag, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (language == selectedLanguage) {
                                StreamStatPill(label = stringResource(R.string.language_current))
                            }
                        }
                    }
                }
            }
            StreamPrimaryButton(
                text = stringResource(R.string.onboarding_continue),
                modifier = Modifier.fillMaxWidth(),
                onClick = onContinue,
            )
        }
    }
}

@Composable
private fun WelcomeStepCard(
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.welcome_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.welcome_secondary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StreamPrimaryButton(
                text = stringResource(R.string.onboarding_continue),
                modifier = Modifier.fillMaxWidth(),
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
private fun LegalStepCard(
    accepted: Boolean,
    submitting: Boolean,
    onToggleAccepted: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.legal_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.legal_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StreamSectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAccepted(!accepted) },
                contentPadding = PaddingValues(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.legal_accept_checkbox),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    StreamStatPill(
                        label = if (accepted) {
                            stringResource(R.string.legal_accept)
                        } else {
                            stringResource(R.string.legal_decline)
                        },
                    )
                }
            }
            StreamPrimaryButton(
                text = stringResource(R.string.legal_accept_continue),
                modifier = Modifier.fillMaxWidth(),
                enabled = accepted,
                isLoading = submitting,
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

private fun progress(step: OnboardingStep): Float {
    return when (step) {
        OnboardingStep.LANGUAGE -> 0.33f
        OnboardingStep.WELCOME -> 0.66f
        OnboardingStep.LEGAL -> 1f
    }
}

private fun stepBadge(step: OnboardingStep): Int {
    return when (step) {
        OnboardingStep.LANGUAGE -> R.string.onboarding_step_language
        OnboardingStep.WELCOME -> R.string.onboarding_step_intro
        OnboardingStep.LEGAL -> R.string.onboarding_step_legal
    }
}

private fun progressBadge(step: OnboardingStep): Int {
    return when (step) {
        OnboardingStep.LANGUAGE -> R.string.onboarding_progress_1
        OnboardingStep.WELCOME -> R.string.onboarding_progress_2
        OnboardingStep.LEGAL -> R.string.onboarding_progress_3
    }
}
