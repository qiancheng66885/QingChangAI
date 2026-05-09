package com.aiaggregator.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiaggregator.app.business.adapter.ChatMessageItem
import com.aiaggregator.app.business.chat.ChatService
import com.aiaggregator.app.data.local.ApiKeyStore
import com.aiaggregator.app.data.local.InMemoryStore
import com.aiaggregator.app.data.model.ContentType
import com.aiaggregator.app.data.model.Message
import com.aiaggregator.app.data.model.MessageRole
import com.aiaggregator.app.data.model.MessageStatus
import com.aiaggregator.app.data.model.ModelCategory
import com.aiaggregator.app.data.model.ModelConfig
import com.aiaggregator.app.data.model.Session
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatService = ChatService()
    private val keyStore = ApiKeyStore(application)

    val sessions: StateFlow<List<Session>> = InMemoryStore.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    var currentSessionId: String = ""
        private set

    private var lastUserPrompt: String? = null
    private var lastUserImageData: Pair<android.net.Uri?, Boolean>? = null

    val currentSessionTitle: String? get() = sessions.value.find { it.id == currentSessionId }?.title

    private val _activeModel = MutableStateFlow<ModelConfig?>(keyStore.getActiveModel())
    val activeModel: StateFlow<ModelConfig?> = _activeModel.asStateFlow()

    val availableModels: List<ModelConfig> get() = keyStore.loadModels()

    private var streamJob: Job? = null

    fun switchModel(model: ModelConfig) {
        _activeModel.value = model
        keyStore.saveModel(model.copy(isDefault = true))
        keyStore.loadModels().map {
            keyStore.saveModel(it.copy(isDefault = it.id == model.id))
        }
    }

    init {
        viewModelScope.launch {
            sessions.collect { list ->
                if (list.isNotEmpty() && currentSessionId.isEmpty()) {
                    // Don't restore old session — start fresh
                    currentSessionId = ""
                }
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val s = Session(id = UUID.randomUUID().toString(), title = "新对话",
                modelConfigId = _activeModel.value?.id ?: "default",
                createdAt = System.currentTimeMillis(), lastActiveAt = System.currentTimeMillis())
            InMemoryStore.insertSession(s)
            switchSession(s.id)
        }
    }

    fun switchSession(sid: String) {
        streamJob?.cancel()
        streamJob = null
        currentSessionId = sid
        viewModelScope.launch {
            InMemoryStore.getMessagesBySession(sid).collect { _messages.value = it }
        }
    }

    fun deleteSession(sid: String) {
        viewModelScope.launch {
            InMemoryStore.deleteSession(sid)
            if (sid == currentSessionId) { _messages.value = emptyList(); currentSessionId = "" }
        }
    }

    fun stopGeneration() {
        streamJob?.cancel()
        streamJob = null
        _messages.update { list ->
            list.map { if (it.status == MessageStatus.STREAMING) it.copy(status = MessageStatus.DONE) else it }
        }
    }

    fun regenerate() {
        val prompt = lastUserPrompt ?: return
        val data = lastUserImageData
        // Remove last assistant message if present
        _messages.update { list ->
            if (list.lastOrNull()?.role == MessageRole.ASSISTANT) list.dropLast(1) else list
        }
        sendMessage(prompt, data?.first, editImage = data?.second ?: false)
    }

    fun sendMessage(text: String, fileUri: android.net.Uri? = null, editImage: Boolean = false) {
        if (text.isBlank() && fileUri == null) return
        lastUserPrompt = text
        lastUserImageData = Pair(fileUri, editImage)
        streamJob?.cancel()
        streamJob = null
        viewModelScope.launch {
            ensureSession(text)
            val sid = currentSessionId; if (sid.isEmpty()) return@launch

            // Read file if attached
            var imageBase64: String? = null
            var imageMimeType: String? = null
            var userContent = text.trim()
            if (fileUri != null) {
                val pair = readFileForUpload(fileUri)
                if (pair != null) {
                    imageBase64 = pair.first
                    imageMimeType = pair.second
                    if (userContent.isBlank()) userContent = "请描述这张图片"
                    // Non-vision chat models can't handle images — warn and strip
                    if (_activeModel.value?.category != ModelCategory.IMAGE) {
                        val warning = Message(id = java.util.UUID.randomUUID().toString(), sessionId = sid,
                            role = MessageRole.ASSISTANT, content = "⚠️ 当前模型不支持图片识别，图片未发送。\n请切换到支持视觉的模型后重试。",
                            timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
                        InMemoryStore.insertMessage(warning)
                        imageBase64 = null; imageMimeType = null
                    }
                }
            }

            val um = Message(
                id = UUID.randomUUID().toString(), sessionId = sid,
                role = MessageRole.USER, content = userContent,
                imageUrl = fileUri?.toString(), // Show attached image in chat bubble
                timestamp = System.currentTimeMillis(), status = MessageStatus.DONE
            )
            InMemoryStore.insertMessage(um)
            InMemoryStore.updateSessionActivity(sid, System.currentTimeMillis())

            val model = _activeModel.value
            val config = model?.let { keyStore.loadPlatforms().find { p -> p.id == it.platformId } }

            if (model?.category == ModelCategory.IMAGE) {
                generateImageFlow(sid, userContent, model, config, imageBase64, imageMimeType, editImage)
            } else {
                startStreamingChat(sid, userContent, config, model, imageBase64, imageMimeType)
            }
        }
    }

    private fun readFileForUpload(uri: android.net.Uri): Pair<String, String>? {
        return try {
            val cr = getApplication<android.app.Application>().contentResolver
            val mimeType = cr.getType(uri) ?: kotlin.run {
                // Fallback: try to guess from file extension or default to image/jpeg
                val path = uri.lastPathSegment ?: ""
                when {
                    path.endsWith(".png") -> "image/png"
                    path.endsWith(".webp") -> "image/webp"
                    path.endsWith(".gif") -> "image/gif"
                    else -> "image/jpeg"
                }
            }
            if (!mimeType.startsWith("image/")) return null
            val bytes = cr.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            Pair(base64, mimeType)
        } catch (e: Exception) { android.util.Log.e("AIAPP", "readFile err: ${e.message}"); null }
    }

    // ---- streaming chat ----

    private fun startStreamingChat(
        sid: String, userText: String,
        config: com.aiaggregator.app.data.model.ApiConfig?, model: ModelConfig?,
        imageBase64: String? = null, imageMimeType: String? = null
    ) {
        if (model == null || config == null ||
            config.baseUrl.isBlank() || config.apiKey.isBlank() || model.modelName.isBlank()
        ) {
            val errMsg = when {
                model == null -> "⚠️ 尚未配置模型\n\n请点击左上角 → 设置 → API 配置，添加平台和模型。"
                config == null -> "⚠️ 模型关联的平台不存在\n\n请重新配置。"
                else -> "⚠️ 配置不完整\n\n请检查平台地址、密钥和模型名称。"
            }
            val rm = Message(id = UUID.randomUUID().toString(), sessionId = sid, role = MessageRole.ASSISTANT,
                content = errMsg, timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
            InMemoryStore.insertMessage(rm)
            return
        }

        val rm = Message(id = UUID.randomUUID().toString(), sessionId = sid, role = MessageRole.ASSISTANT,
            content = "", timestamp = System.currentTimeMillis(), status = MessageStatus.STREAMING,
            modelName = model?.let { m -> m.displayName.ifBlank { m.modelName } })
        InMemoryStore.insertMessage(rm)

        streamJob = viewModelScope.launch {
            try {
                val history = _messages.value
                    .filter { it.role != MessageRole.SYSTEM && it.id != rm.id }
                    .let { if (it.size > 40) it.takeLast(40) else it } // trim to last 40 msgs
                    .map { ChatMessageItem(it.role.name.lowercase(), it.content) } +
                    ChatMessageItem("user", userText, imageBase64 = imageBase64, imageMimeType = imageMimeType)
                val cfg = config.copy()

                withContext(Dispatchers.IO) {
                    chatService.streamChat(history, cfg, model.modelName).collect { chunk ->
                        when {
                            chunk.error != null -> finishWithError(rm, chunk.error)
                            chunk.isDone -> {
                            if (chunk.usage != null) {
                                _messages.update { list -> list.map { if (it.id == rm.id) it.copy(promptTokens = chunk.usage!!.promptTokens, completionTokens = chunk.usage!!.completionTokens, tokenCount = chunk.usage!!.totalTokens) else it } }
                            }
                            finishStreaming(rm)
                        }
                            chunk.content != null -> appendContent(rm, chunk.content!!)
                        }
                    }
                }
            } catch (e: CancellationException) {
                // user pressed stop — keep whatever was streamed so far
                finishStreaming(rm)
            } catch (e: UnknownHostException) {
                finishWithError(rm, "无法连接: ${e.message}")
            } catch (e: ConnectException) {
                finishWithError(rm, "连接被拒绝")
            } catch (e: SocketTimeoutException) {
                finishWithError(rm, "请求超时")
            } catch (e: java.io.IOException) {
                finishWithError(rm, "网络异常: ${e.message}")
            } catch (e: Exception) {
                finishWithError(rm, e.message ?: "未知错误")
            }
        }
    }

    private fun appendContent(rm: Message, delta: String) {
        _messages.update { list ->
            list.map { if (it.id == rm.id) it.copy(content = it.content + delta) else it }
        }
    }

    private fun finishStreaming(rm: Message) {
        _messages.update { list ->
            list.map { if (it.id == rm.id) it.copy(status = MessageStatus.DONE) else it }
        }
        val final = _messages.value.find { it.id == rm.id } ?: rm
        InMemoryStore.insertMessage(final)
    }

    private fun finishWithError(rm: Message, error: String) {
        val current = _messages.value.find { it.id == rm.id }
        val prefix = if (current?.content?.isNotBlank() == true) current.content + "\n\n" else ""
        val display = "$prefix❌ $error"
        _messages.update { list ->
            list.map {
                if (it.id == rm.id)
                    it.copy(content = display, status = MessageStatus.DONE, contentType = ContentType.ERROR)
                else it
            }
        }
        val final = _messages.value.find { it.id == rm.id } ?: rm.copy(content = display, status = MessageStatus.DONE)
        InMemoryStore.insertMessage(final)
    }

    // ---- image generation ----

    private fun generateImageFlow(
        sid: String, prompt: String,
        model: ModelConfig, config: com.aiaggregator.app.data.model.ApiConfig?,
        imageBase64: String? = null, imageMimeType: String? = null,
        editImage: Boolean = false
    ) {
        if (config == null) {
            val rm = Message(id = UUID.randomUUID().toString(), sessionId = sid, role = MessageRole.ASSISTANT,
                content = "⚠️ 尚未配置图片生成平台", timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
            InMemoryStore.insertMessage(rm)
            return
        }

        val rm = Message(id = UUID.randomUUID().toString(), sessionId = sid, role = MessageRole.ASSISTANT,
            content = "", timestamp = System.currentTimeMillis(), status = MessageStatus.STREAMING,
            modelName = model.displayName.ifBlank { model.modelName })
        InMemoryStore.insertMessage(rm)

        viewModelScope.launch {
            try {
                val result = when {
                    // User attached an image → try edit endpoint first
                    imageBase64 != null -> {
                        appendContent(rm, "正在根据上传的图片生成...\n")
                        withContext(Dispatchers.IO) {
                            val imgBytes = android.util.Base64.decode(imageBase64, android.util.Base64.NO_WRAP)
                            val editResult = chatService.editImage(prompt, model.modelName, imgBytes, config)
                            if (editResult.error != null) {
                                // Edit failed (maybe model doesn't support it), fall back to generate
                                chatService.generateImage(prompt, model.modelName, config)
                            } else editResult
                        }
                    }
                    // Edit only when toggle is ON and there's a previous image
                    editImage && _messages.value.lastOrNull { it.role == MessageRole.ASSISTANT && !it.imageUrl.isNullOrBlank() } != null -> {
                        val lastUrl = _messages.value.last { it.role == MessageRole.ASSISTANT && !it.imageUrl.isNullOrBlank() }.imageUrl!!
                        appendContent(rm, "正在修改图片...\n")
                        withContext(Dispatchers.IO) {
                            val imgBytes = downloadImage(lastUrl)
                            if (imgBytes != null) chatService.editImage(prompt, model.modelName, imgBytes, config)
                            else chatService.generateImage(prompt, model.modelName, config)
                        }
                    }
                    // Fresh generation
                    else -> withContext(Dispatchers.IO) { chatService.generateImage(prompt, model.modelName, config) }
                }
                if (result.error != null) {
                    finishWithError(rm, result.error)
                } else if (result.urls.isEmpty()) {
                    finishWithError(rm, "未返回图片")
                } else {
                    val url = result.urls.first()
                    val final = rm.copy(
                        content = "生成图片", imageUrl = url,
                        status = MessageStatus.DONE, contentType = ContentType.IMAGE
                    )
                    _messages.update { list -> list.map { if (it.id == rm.id) final else it } }
                    InMemoryStore.insertMessage(final)
                }
            } catch (e: Exception) {
                val detail = when {
                    e.message != null -> e.message!!
                    e is java.io.IOException -> "网络连接失败"
                    else -> "未知错误 (${e.javaClass.simpleName})"
                }
                finishWithError(rm, "图片处理失败: $detail")
            }
        }
    }

    private suspend fun downloadImage(url: String): ByteArray? {
        return try {
            val client = com.aiaggregator.app.data.remote.HttpClientFactory.create()
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.bytes() else null
        } catch (_: Exception) { null }
    }

    // ---- helpers ----

    private suspend fun ensureSession(text: String) {
        if (currentSessionId.isNotEmpty()) return
        val s = Session(
            id = UUID.randomUUID().toString(), title = text.take(20),
            modelConfigId = _activeModel.value?.id ?: "default",
            createdAt = System.currentTimeMillis(), lastActiveAt = System.currentTimeMillis()
        )
        InMemoryStore.insertSession(s)
        currentSessionId = s.id
        switchSession(s.id)
        delay(50)
    }
}
