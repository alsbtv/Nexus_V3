package com.hiyako.app.ui

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hiyako.app.AiViewModel
import com.hiyako.app.puter.PuterWebView
import com.hiyako.app.ui.components.ChatInputBar
import com.hiyako.app.ui.components.ClearChatDialog
import com.hiyako.app.ui.components.EmptyStateGrid
import com.hiyako.app.ui.components.ErrorBanner
import com.hiyako.app.ui.components.MessageList
import com.hiyako.app.ui.components.ModelSheet
import com.hiyako.app.ui.components.NexusBackground
import com.hiyako.app.ui.components.NexusDrawer
import com.hiyako.app.ui.components.NexusTopBar
import com.hiyako.app.ui.components.UpdateSheet
import com.hiyako.app.ui.components.WorkspaceSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: AiViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    var input by rememberSaveable { mutableStateOf("") }
    var showModels by rememberSaveable { mutableStateOf(false) }
    var showWorkspace by rememberSaveable { mutableStateOf(false) }
    var showUpdate by rememberSaveable { mutableStateOf(false) }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    LaunchedEffect(webView) {
        val target = webView ?: return@LaunchedEffect
        viewModel.commands.collect { js ->
            // The engine can be torn down at any moment (renderer death, activity
            // recreation). A failed dispatch must never crash the UI thread.
            runCatching { target.evaluateJavascript(js, null) }
                .onFailure { android.util.Log.w("NexusAI", "JS dispatch failed", it) }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            delay(6500)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(state.messages.size) {
        val count = listState.layoutInfo.totalItemsCount
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    LaunchedEffect(state.streamingText) {
        if (state.isGenerating && listState.isNearBottom()) {
            val count = listState.layoutInfo.totalItemsCount
            if (count > 0) listState.scrollToItem(count - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.66f),
        drawerContent = {
            NexusDrawer(
                sessions = state.sessions,
                currentSessionId = state.currentSessionId,
                userName = state.userName,
                isSignedIn = state.isSignedIn,
                totalMessages = state.totalMessages,
                onClose = { scope.launch { drawerState.close() } },
                onNewChat = {
                    viewModel.newChat()
                    input = ""
                    focusManager.clearFocus()
                    scope.launch { drawerState.close() }
                },
                onOpenSession = { id ->
                    viewModel.openSession(id)
                    focusManager.clearFocus()
                    scope.launch { drawerState.close() }
                },
                onDeleteSession = viewModel::deleteSession,
                onWorkspace = {
                    showWorkspace = true
                    scope.launch { drawerState.close() }
                },
                onUpdate = {
                    showUpdate = true
                    scope.launch { drawerState.close() }
                },
                onClearChat = {
                    showClearDialog = true
                    scope.launch { drawerState.close() }
                },
                onSignIn = viewModel::signIn,
                onSignOut = viewModel::signOut
            )
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            NexusBackground()

            Column(
                Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                NexusTopBar(
                    title = state.currentTitle,
                    isGenerating = state.isGenerating,
                    isSignedIn = state.isSignedIn,
                    onMenu = { scope.launch { drawerState.open() } },
                    onStatusClick = {
                        if (state.isSignedIn) {
                            scope.launch { drawerState.open() }
                        } else {
                            viewModel.signIn()
                        }
                    },
                    modifier = Modifier.statusBarsPadding()
                )

                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 }
                ) {
                    state.error?.let { message ->
                        ErrorBanner(message = message, onDismiss = viewModel::dismissError)
                    }
                }

                Box(Modifier.weight(1f)) {
                    if (state.messages.isEmpty() && !state.isGenerating) {
                        EmptyStateGrid(
                            onAction = { prompt ->
                                input = prompt
                                // requestFocus() throws if the field hasn't been
                                // placed yet — keep the tap harmless.
                                runCatching { focusRequester.requestFocus() }
                            }
                        )
                    } else {
                        MessageList(
                            messages = state.messages,
                            isGenerating = state.isGenerating,
                            streamingText = state.streamingText,
                            listState = listState
                        )
                    }
                }

                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(top = 6.dp, bottom = 8.dp)
                ) {
                    ChatInputBar(
                        value = input,
                        onValueChange = { input = it },
                        model = state.selectedModel,
                        isGenerating = state.isGenerating,
                        isSignedIn = state.isSignedIn,
                        onSend = {
                            val prompt = input
                            if (prompt.isNotBlank()) {
                                viewModel.sendMessage(prompt)
                                input = ""
                            }
                        },
                        onStop = viewModel::stopGeneration,
                        onModelClick = {
                            focusManager.clearFocus()
                            showModels = true
                        },
                        onSignIn = viewModel::signIn,
                        focusRequester = focusRequester
                    )
                }
            }

            PuterWebView(
                events = viewModel,
                modifier = Modifier
                            .size(1.dp)
                            .alpha(0f)
                            .align(Alignment.TopStart),
                onWebViewReady = { webView = it }
            )
        }
    }

    if (showModels) {
        ModelSheet(
            models = state.availableModels,
            selected = state.selectedModel,
            onSelect = { model ->
                viewModel.selectModel(model)
                showModels = false
            },
            onDismiss = { showModels = false }
        )
    }

    if (showWorkspace) {
        WorkspaceSheet(
            chatCount = state.sessions.size,
            messageCount = state.totalMessages,
            modelCount = state.availableModels.size,
            onNewChat = {
                viewModel.newChat()
                input = ""
                showWorkspace = false
            },
            onClearChat = {
                showWorkspace = false
                showClearDialog = true
            },
            onDismiss = { showWorkspace = false }
        )
    }

    if (showUpdate) {
        UpdateSheet(onDismiss = { showUpdate = false })
    }

    if (showClearDialog) {
        ClearChatDialog(
            onConfirm = viewModel::clearCurrentChat,
            onDismiss = { showClearDialog = false }
        )
    }
}

private fun LazyListState.isNearBottom(): Boolean {
    val info = layoutInfo
    val lastVisible = info.visibleItemsInfo.lastOrNull() ?: return true
    return lastVisible.index >= info.totalItemsCount - 2
}
