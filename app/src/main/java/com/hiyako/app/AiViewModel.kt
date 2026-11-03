package com.hiyako.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hiyako.app.data.ChatDao
import com.hiyako.app.data.ChatDatabase
import com.hiyako.app.data.ChatRole
import com.hiyako.app.data.ChatSessionEntity
import com.hiyako.app.data.MessageEntity
import com.hiyako.app.data.MessageType
import com.hiyako.app.puter.PuterEvents
import com.hiyako.app.ui.theme.ElectricBlue
import com.hiyako.app.ui.theme.ElectricViolet
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.NeonCyanMuted
import com.hiyako.app.ui.theme.NeonMagenta
import com.hiyako.app.ui.theme.NeonMagentaMuted
import com.hiyako.app.ui.theme.SignalGreen
import com.hiyako.app.ui.theme.SolarAmber
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/** One selectable intelligence, surfaced in the model sheet. */
data class ModelOption(
    val id: String,
    val name: String,
    val provider: String,
    val tagline: String,
    val accent: Color
)

/**
 * The curated catalogue. Puter reports its own model list at runtime and we
 * merge anything new on top, but these keep the app beautiful before the
 * network answers.
 */
val DEFAULT_MODELS: List<ModelOption> = listOf(
    ModelOption("gpt-4o-mini", "GPT-4o mini", "OpenAI", "Fast · everyday", NeonCyan),
    ModelOption("gpt-4o", "GPT-4o", "OpenAI", "Flagship reasoning", ElectricBlue),
    ModelOption("claude-sonnet-4", "Claude Sonnet 4", "Anthropic", "Long-form thinking", NeonMagenta),
    ModelOption("claude-3-7-sonnet", "Claude 3.7 Sonnet", "Anthropic", "Deep analysis", NeonMagentaMuted),
    ModelOption("deepseek-chat", "DeepSeek V3", "DeepSeek", "Code · math", ElectricViolet),
    ModelOption("deepseek-reasoner", "DeepSeek R1", "DeepSeek", "Step-by-step logic", SignalGreen),
    ModelOption("google/gemini-2.0-flash", "Gemini 2.0 Flash", "Google", "Blazing fast", SolarAmber),
    ModelOption("mistral-large-latest", "Mistral Large", "Mistral", "Multilingual", ElectricBlue),
    ModelOption("meta-llama/llama-3.1-70b-instruct", "Llama 3.1 70B", "Meta", "Open weights", NeonCyanMuted)
)

private val MODEL_ACCENTS = listOf(
    NeonCyan,
    ElectricBlue,
    NeonMagenta,
    ElectricViolet,
    SignalGreen,
    SolarAmber,
    NeonCyanMuted
)

/** UI model of a persisted message (or an in-flight one). */
data class ChatMessageUi(
    val id: Long,
    val role: String,
    val content: String,
    val type: String,
    val model: String?,
    val timestamp: Long
) {
    val isUser: Boolean get() = role == ChatRole.USER
}

data class AiUiState(
    val sessions: List<ChatSessionEntity> = emptyList(),
    val currentSessionId: Long? = null,
    val currentTitle: String = "New conversation",
    val messages: List<ChatMessageUi> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingText: String = "",
    val selectedModel: ModelOption = DEFAULT_MODELS.first(),
    val availableModels: List<ModelOption> = DEFAULT_MODELS,
    val isSignedIn: Boolean = false,
    val userName: String? = null,
    val isWebReady: Boolean = false,
    val totalMessages: Int = 0,
    val error: String? = null
)

/**
 * Single source of truth for the chat. Owns Room persistence, the active
 * session, streaming state and the JavaScript bridge events coming from the
 * invisible Puter WebView.
 */
class AiViewModel(application: Application) : AndroidViewModel(application), PuterEvents {

    private val dao: ChatDao = ChatDatabase.get(application.applicationContext).chatDao()
    private val prefs = application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val initialModel: ModelOption =
        DEFAULT_MODELS.firstOrNull { it.id == prefs.getString(KEY_MODEL, null) }
            ?: DEFAULT_MODELS.first()

    private val _uiState = MutableStateFlow(AiUiState(selectedModel = initialModel))
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    /** Raw JavaScript handed to the WebView (sign-in, fetch models, ask). */
    private val _commands = Channel<String>(Channel.BUFFERED)
    val commands: Flow<String> = _commands.receiveAsFlow()

    /**
     * Storage work runs on IO and can never take the process down: a failed
     * Room query surfaces as a friendly banner instead of a crash.
     */
    private val safeStorage = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Storage task failed", throwable)
        _uiState.update {
            it.copy(error = "Local storage hiccup — please try that again.")
        }
    }

    private var messageJob: Job? = null
    private var stopRequested = false

    init {
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            dao.observeSessions().collect { sessions ->
                _uiState.update { state ->
                    val active = sessions.firstOrNull { it.id == state.currentSessionId }
                    state.copy(
                        sessions = sessions,
                        currentTitle = active?.title ?: state.currentTitle
                    )
                }
            }
        }
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            dao.observeMessageCount().collect { count ->
                _uiState.update { it.copy(totalMessages = count) }
            }
        }
    }

    // ---------------------------------------------------------------- chats

    fun newChat() {
        messageJob?.cancel()
        stopRequested = false
        _uiState.update {
            it.copy(
                currentSessionId = null,
                currentTitle = "New conversation",
                messages = emptyList(),
                isGenerating = false,
                streamingText = "",
                error = null
            )
        }
    }

    fun openSession(sessionId: Long) {
        val session = _uiState.value.sessions.firstOrNull { it.id == sessionId } ?: return
        stopRequested = false
        _uiState.update {
            it.copy(
                currentSessionId = sessionId,
                currentTitle = session.title,
                messages = emptyList(),
                isGenerating = false,
                streamingText = "",
                error = null
            )
        }
        observeMessages(sessionId)
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            dao.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) newChat()
        }
    }

    fun clearCurrentChat() {
        val sessionId = _uiState.value.currentSessionId ?: return
        stopRequested = true
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            _commands.send("window.stopGeneration();")
            dao.clearMessages(sessionId)
            _uiState.update { it.copy(isGenerating = false, streamingText = "", error = null) }
        }
    }

    private fun observeMessages(sessionId: Long) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch(safeStorage + Dispatchers.IO) {
            dao.observeMessages(sessionId).collect { rows ->
                _uiState.update { it.copy(messages = rows.map { row -> row.toUi() }) }
            }
        }
    }

    // --------------------------------------------------------------- sending

    fun sendMessage(rawPrompt: String) {
        val prompt = rawPrompt.trim()
        val snapshot = _uiState.value
        if (prompt.isEmpty() || snapshot.isGenerating) return

        if (!snapshot.isWebReady) {
            _uiState.update {
                it.copy(error = "Nexus core is still booting. Give it a second and send again.")
            }
            return
        }

        stopRequested = false
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            val existingId = _uiState.value.currentSessionId
            var sessionId: Long = existingId ?: 0L
            if (existingId == null) {
                val title = buildTitle(prompt)
                sessionId = dao.insertSession(ChatSessionEntity(title = title))
                _uiState.update { it.copy(currentSessionId = sessionId, currentTitle = title) }
                observeMessages(sessionId)
            }

            val activeSessionId = sessionId
            val model = _uiState.value.selectedModel

            dao.insertMessage(
                MessageEntity(
                    sessionId = activeSessionId,
                    role = ChatRole.USER,
                    content = prompt,
                    type = MessageType.TEXT
                )
            )
            touchSession(activeSessionId)

            _uiState.update { it.copy(isGenerating = true, streamingText = "", error = null) }

            val history = dao.getMessages(activeSessionId).takeLast(16)
            val payload = JSONArray()
            history.forEach { message ->
                if (message.type == MessageType.TEXT || message.type == MessageType.IMAGE) {
                    payload.put(
                        JSONObject()
                            .put("role", message.role)
                            .put("content", message.content)
                    )
                }
            }

            val js = "window.askPuter($payload, ${JSONObject.quote(model.id)});"
            _commands.send(js)
        }
    }

    fun stopGeneration() {
        val state = _uiState.value
        if (!state.isGenerating) return
        stopRequested = true
        viewModelScope.launch { _commands.send("window.stopGeneration();") }
        finishGeneration(state.streamingText)
    }

    private fun finishGeneration(content: String) {
        val state = _uiState.value
        if (!state.isGenerating) return
        val sessionId = state.currentSessionId
        _uiState.update { it.copy(isGenerating = false, streamingText = "") }

        if (sessionId == null) return
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            val body = content.trim()
            if (body.isNotEmpty()) {
                dao.insertMessage(
                    MessageEntity(
                        sessionId = sessionId,
                        role = ChatRole.ASSISTANT,
                        content = body,
                        model = state.selectedModel.name
                    )
                )
            } else if (stopRequested) {
                dao.insertMessage(
                    MessageEntity(
                        sessionId = sessionId,
                        role = ChatRole.ASSISTANT,
                        content = "Generation stopped.",
                        model = state.selectedModel.name
                    )
                )
            } else {
                dao.insertMessage(
                    MessageEntity(
                        sessionId = sessionId,
                        role = ChatRole.ASSISTANT,
                        content = "I couldn't produce a response. Try again, or pick another model.",
                        type = MessageType.ERROR
                    )
                )
            }
            touchSession(sessionId)
        }
        stopRequested = false
    }

    private fun touchSession(sessionId: Long) {
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            dao.getSession(sessionId)?.let { session ->
                dao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    // ---------------------------------------------------------------- models

    fun selectModel(model: ModelOption) {
        _uiState.update { it.copy(selectedModel = model) }
        prefs.edit().putString(KEY_MODEL, model.id).apply()
    }

    // ------------------------------------------------------------- auth + JS

    fun signIn() {
        viewModelScope.launch { _commands.send("window.startSignIn();") }
    }

    fun signOut() {
        viewModelScope.launch { _commands.send("window.signOut();") }
        _uiState.update { it.copy(isSignedIn = false, userName = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onWebReady() {
        _uiState.update { it.copy(isWebReady = true) }
        viewModelScope.launch {
            _commands.send("window.checkAuth();")
            _commands.send("window.fetchModels();")
        }
    }

    override fun onAuth(userJson: String) {
        val json = runCatching { JSONObject(userJson) }.getOrNull()
        val handle = json?.optString("username")?.takeIf { it.isNotBlank() }
            ?: json?.optString("email")?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Creator"
        _uiState.update { it.copy(isSignedIn = true, userName = handle, error = null) }
        viewModelScope.launch { _commands.send("window.fetchModels();") }
    }

    override fun onAuthState(state: String) {
        when (state) {
            "signed_out" -> _uiState.update { it.copy(isSignedIn = false, userName = null) }
            "unavailable" -> _uiState.update {
                it.copy(error = "Puter.js couldn't load. Check your internet connection.")
            }
        }
    }

    override fun onModels(json: String) {
        val array = runCatching { JSONArray(json) }.getOrNull() ?: return
        if (array.length() == 0) return

        val incoming = mutableListOf<ModelOption>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id")
            if (id.isBlank()) continue
            val name = item.optString("name").ifBlank { id.substringAfterLast('/') }
            val provider = item.optString("provider").ifBlank { "Puter" }
            incoming += ModelOption(
                id = id,
                name = name,
                provider = provider,
                tagline = "Puter AI",
                accent = MODEL_ACCENTS[index % MODEL_ACCENTS.size]
            )
        }
        if (incoming.isEmpty()) return

        val curatedIds = DEFAULT_MODELS.map { it.id }.toSet()
        val merged = (DEFAULT_MODELS + incoming.filter { it.id !in curatedIds }).take(40)
        val selected = merged.firstOrNull { it.id == _uiState.value.selectedModel.id } ?: merged.first()
        _uiState.update { it.copy(availableModels = merged, selectedModel = selected) }
    }

    override fun onToken(token: String) {
        if (!_uiState.value.isGenerating) return
        _uiState.update { it.copy(streamingText = it.streamingText + token) }
    }

    override fun onComplete(fullText: String) {
        val state = _uiState.value
        if (!state.isGenerating) return
        finishGeneration(fullText.ifBlank { state.streamingText })
    }

    override fun onError(message: String) {
        val state = _uiState.value
        if (stopRequested) {
            stopRequested = false
            return
        }
        val friendly = humanizeError(message)
        if (!state.isGenerating) {
            _uiState.update { it.copy(error = friendly) }
            return
        }

        val sessionId = state.currentSessionId
        _uiState.update { it.copy(isGenerating = false, streamingText = "") }
        if (sessionId == null) {
            _uiState.update { it.copy(error = friendly) }
            return
        }
        viewModelScope.launch(safeStorage + Dispatchers.IO) {
            val partial = state.streamingText.trim()
            if (partial.isNotEmpty()) {
                dao.insertMessage(
                    MessageEntity(
                        sessionId = sessionId,
                        role = ChatRole.ASSISTANT,
                        content = partial,
                        model = state.selectedModel.name
                    )
                )
            }
            dao.insertMessage(
                MessageEntity(
                    sessionId = sessionId,
                    role = ChatRole.ASSISTANT,
                    content = friendly,
                    type = MessageType.ERROR
                )
            )
            touchSession(sessionId)
        }
    }

    override fun onStopped() {
        if (!_uiState.value.isGenerating) return
        finishGeneration(_uiState.value.streamingText)
    }

    override fun onLog(message: String) {
        Log.d(TAG, message)
    }

    // --------------------------------------------------------------- helpers

    private fun MessageEntity.toUi() = ChatMessageUi(
        id = id,
        role = role,
        content = content,
        type = type,
        model = model,
        timestamp = timestamp
    )

    private fun humanizeError(raw: String): String {
        val message = raw.trim().ifEmpty { "Something went wrong." }
        return when {
            message.contains("sign in", ignoreCase = true) ||
                message.contains("auth", ignoreCase = true) ||
                message.contains("unauthorized", ignoreCase = true) ||
                message.contains("401") ->
                "Sign in to Puter to keep chatting — open the menu and tap “Connect Puter”."

            message.contains("model", ignoreCase = true) &&
                message.contains("not", ignoreCase = true) ->
                "That model isn't available on your Puter account. Try another one."

            message.contains("network", ignoreCase = true) ||
                message.contains("fetch", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ->
                "Network hiccup — check your connection and send again."

            message.contains("quota", ignoreCase = true) ||
                message.contains("limit", ignoreCase = true) ->
                "Your Puter usage limit was reached. It resets soon — try a lighter model."

            else -> message.take(260)
        }
    }

    companion object {
        private const val TAG = "NexusAI"
        private const val PREFS = "nexus_prefs"
        private const val KEY_MODEL = "selected_model"

        /** Creates a short, human title from the first prompt. */
        fun buildTitle(prompt: String): String {
            val clean = prompt.trim().replace(Regex("\\s+"), " ")
            if (clean.isEmpty()) return "New conversation"
            val sentence = clean.substringBefore('.').substringBefore('?').substringBefore('!')
            val words = sentence.split(' ')
                .filter { it.isNotBlank() }
                .take(7)
                .joinToString(" ")
                .ifBlank { clean }
            val title = if (words.length > 46) words.take(44).trimEnd() + "…" else words
            return title.replaceFirstChar { it.uppercaseChar() }
        }
    }
}
