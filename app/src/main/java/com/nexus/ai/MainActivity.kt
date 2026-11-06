package com.nexus.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.ai.ui.components.AppDrawer
import com.nexus.ai.ui.screens.ChatScreen
import com.nexus.ai.ui.screens.SettingsScreen
import com.nexus.ai.ui.theme.NexusTheme
import com.nexus.ai.ui.viewmodel.DrawerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexusTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NexusApp()
                }
            }
        }
    }
}

@Composable
fun NexusApp() {
    val drawerViewModel: DrawerViewModel = viewModel()
    var currentScreen by remember { mutableStateOf("chat") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (currentScreen == "settings") {
        SettingsScreen(onBackClick = { currentScreen = "chat" })
    } else {
        AppDrawer(
            drawerState = drawerState,
            viewModel = drawerViewModel,
            onNavigateToSettings = { scope.launch { drawerState.close() }; currentScreen = "settings" },
            onNewChatClick = { scope.launch { drawerState.close() } }
        ) {
            ChatScreen(onMenuClick = { scope.launch { drawerState.open() } })
        }
    }
}
