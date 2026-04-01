package com.streamgram.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamgram.app.RootUiState
import com.streamgram.core.i18n.R
import com.streamgram.core.model.AuthState
import com.streamgram.core.navigation.MainRoutes
import com.streamgram.feature.onboarding.OnboardingRoute
import com.streamgram.feature.auth.AuthRoute
import com.streamgram.feature.calls.CallsRoute
import com.streamgram.feature.channels.ChannelsRoute
import com.streamgram.feature.channels.ChannelProfileRoute
import com.streamgram.feature.chat.ChatRoute
import com.streamgram.feature.chat.ForwardPostRoute
import com.streamgram.feature.chatlist.ChatListRoute
import com.streamgram.feature.comments.CommentsRoute
import com.streamgram.feature.createchannel.CreateChannelRoute
import com.streamgram.feature.createchat.CreateChatRoute
import com.streamgram.feature.language.LanguageRoute
import com.streamgram.feature.notifications.NotificationsRoute
import com.streamgram.feature.privacy.PrivacyRoute
import com.streamgram.feature.profile.ProfileRoute
import com.streamgram.feature.sessions.SessionsRoute
import com.streamgram.feature.settings.DeleteAccountRoute
import com.streamgram.feature.settings.SettingsRoute

@Composable
fun StreamGramApp(
    rootState: RootUiState,
) {
    when {
        rootState.showSplash -> SplashRoute()
        rootState.shouldShowOnboarding -> OnboardingRoute()
        rootState.authorizationState is AuthState.Authenticated -> MainShell()
        else -> AuthRoute(onAuthenticated = {})
    }
}

@Composable
private fun SplashRoute() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.splash_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MainShell() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val destinations = listOf(
        TopLevelDestination(
            route = MainRoutes.Chats,
            label = stringResource(R.string.nav_chats),
            icon = Icons.AutoMirrored.Filled.Send,
        ),
        TopLevelDestination(
            route = MainRoutes.Channels,
            label = stringResource(R.string.nav_channels),
            icon = Icons.Filled.Home,
        ),
        TopLevelDestination(
            route = MainRoutes.Account,
            label = stringResource(R.string.nav_account),
            icon = Icons.Filled.Person,
        ),
    )

    val showBottomBar = destinations.any { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    destinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoutes.Chats,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MainRoutes.Chats) {
                ChatListRoute(
                    onOpenChat = { chatId -> navController.navigate(MainRoutes.chatDetail(chatId)) },
                    onOpenChannel = { channelId -> navController.navigate(MainRoutes.channelDetail(channelId)) },
                    onCreateChat = { navController.navigate(MainRoutes.CreateChat) },
                )
            }
            composable(MainRoutes.Channels) {
                ChannelsRoute(
                    onOpenChannel = { channelId -> navController.navigate(MainRoutes.channelDetail(channelId)) },
                    onCreateChannel = { navController.navigate(MainRoutes.CreateChannel) },
                )
            }
            composable(MainRoutes.Account) {
                ProfileRoute(
                    onOpenSettings = { navController.navigate(MainRoutes.Settings) },
                    onOpenNotifications = { navController.navigate(MainRoutes.Notifications) },
                )
            }
            composable(MainRoutes.Notifications) {
                NotificationsRoute(
                    onBack = navController::popBackStack,
                )
            }
            composable(MainRoutes.Settings) {
                SettingsRoute(
                    onBack = navController::popBackStack,
                    onOpenLanguage = { navController.navigate(MainRoutes.Language) },
                    onOpenPrivacy = { navController.navigate(MainRoutes.Privacy) },
                    onOpenSessions = { navController.navigate(MainRoutes.Sessions) },
                    onOpenDeleteAccount = { navController.navigate(MainRoutes.DeleteAccount) },
                )
            }
            composable(MainRoutes.DeleteAccount) {
                DeleteAccountRoute(
                    onBack = navController::popBackStack,
                )
            }
            composable(MainRoutes.Privacy) {
                PrivacyRoute(
                    onBack = navController::popBackStack,
                )
            }
            composable(MainRoutes.Sessions) {
                SessionsRoute(
                    onBack = navController::popBackStack,
                )
            }
            composable(MainRoutes.CreateChat) {
                CreateChatRoute(
                    onBack = navController::popBackStack,
                    onCreated = { chatId ->
                        navController.popBackStack()
                        navController.navigate(MainRoutes.chatDetail(chatId))
                    },
                )
            }
            composable(MainRoutes.CreateChannel) {
                CreateChannelRoute(
                    onBack = navController::popBackStack,
                    onCreated = { channelId ->
                        navController.popBackStack()
                        navController.navigate(MainRoutes.channelDetail(channelId))
                    },
                )
            }
            composable(
                route = MainRoutes.CommentsPattern,
                arguments = listOf(navArgument("postId") { type = NavType.StringType }),
            ) {
                CommentsRoute(
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = MainRoutes.ChannelDetailPattern,
                arguments = listOf(navArgument("channelId") { type = NavType.StringType }),
            ) {
                ChannelProfileRoute(
                    onBack = navController::popBackStack,
                    onForwardPost = { postId -> navController.navigate(MainRoutes.forward(postId)) },
                )
            }
            composable(
                route = MainRoutes.ChatDetailPattern,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
            ) {
                ChatRoute(
                    onBack = navController::popBackStack,
                    onOpenCall = { navController.navigate(MainRoutes.Calls) },
                )
            }
            composable(MainRoutes.Calls) {
                CallsRoute(
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = MainRoutes.CallJoinPattern,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType },
                ),
            ) {
                CallsRoute(
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = MainRoutes.ForwardPattern,
                arguments = listOf(navArgument("postId") { type = NavType.StringType }),
            ) {
                ForwardPostRoute(
                    onBack = navController::popBackStack,
                    onForwarded = { navController.popBackStack() },
                )
            }
            composable(MainRoutes.Language) {
                LanguageRoute(
                    onBack = navController::popBackStack,
                )
            }
        }
    }
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)
