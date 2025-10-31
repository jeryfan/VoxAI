package com.voxai.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voxai.presentation.aichat.AIChatScreen
import com.voxai.presentation.settings.SettingsScreen
import com.voxai.presentation.voicechanger.VoiceChangerScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object VoiceChanger : Screen("voice_changer", "变声器", Icons.Default.Mic)
    object AIChat : Screen("ai_chat", "AI聊天", Icons.Default.Chat)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoxAIApp() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.VoiceChanger,
        Screen.AIChat,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.VoiceChanger.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.VoiceChanger.route) {
                VoiceChangerScreen()
            }
            composable(Screen.AIChat.route) {
                AIChatScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
