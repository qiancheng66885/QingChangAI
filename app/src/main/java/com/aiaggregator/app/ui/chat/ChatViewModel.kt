package com.aiaggregator.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiaggregator.app.business.adapter.ChatMessageItem
import com.aiaggregator.app.business.adapter.ImagePart
import com.aiaggregator.app.business.adapter.ImageGenResult
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
import kotlinx.coroutines.withTimeout
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
    // Independent scope for image generation — survives screen switches
    private val imageGenSupervisor = kotlinx.coroutines.SupervisorJob()
    private val imageGenScope = kotlinx.coroutines.CoroutineScope(imageGenSupervisor + Dispatchers.Main)

    val sessions: StateFlow<List<Session>> = InMemoryStore.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    var currentSessionId: String = ""
        private set

    private var lastUserPrompt: String? = null
    private var lastUserImageData: Pair<List<android.net.Uri>, Boolean>? = null

    val currentSessionTitle: String? get() = sessions.value.find { it.id == currentSessionId }?.title

    private val _activeModel = MutableStateFlow<ModelConfig?>(keyStore.getActiveModel())
    val activeModel: StateFlow<ModelConfig?> = _activeModel.asStateFlow()

    val availableModels: List<ModelConfig> get() = keyStore.loadModels()

    private var streamJob: Job? = null
    private var sessionCollectorJob: Job? = null
    private var imageGenJob: Job? = null
    private var hintJob: Job? = null

    /** 图片生成状态文本 — UI 观察它来显示轮播提示 */
    private val _genHint = MutableStateFlow("")
    val genHint: StateFlow<String> = _genHint.asStateFlow()

    private val genHints = listOf(
        "构思画面中...", "正在细化细节...", "调整色彩与光影...",
        "渲染中，请稍候...", "优化画面构图...", "快完成了..."
    )

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
        // Don't cancel imageGenJob/hintJob — let generation survive session switch
        sessionCollectorJob?.cancel()
        currentSessionId = sid
        _genHint.value = ""
        sessionCollectorJob = viewModelScope.launch {
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
        imageGenJob?.cancel()
        imageGenJob = null
        hintJob?.cancel()
        hintJob = null
        _genHint.value = ""
        _messages.update { list ->
            list.map { if (it.status == MessageStatus.STREAMING) it.copy(status = MessageStatus.DONE) else it }
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageGenSupervisor.cancel()
    }

    fun regenerate() {
        val prompt = lastUserPrompt ?: return
        val data = lastUserImageData
        _messages.update { list ->
            if (list.lastOrNull()?.role == MessageRole.ASSISTANT) list.dropLast(1) else list
        }
        sendMessage(prompt, data?.first ?: emptyList(), editImage = data?.second ?: false)
    }

    fun sendMessage(text: String, fileUris: List<android.net.Uri> = emptyList(), editImage: Boolean = false) {
        if (text.isBlank() && fileUris.isEmpty()) return
        lastUserPrompt = text
        lastUserImageData = Pair(fileUris, editImage)
        streamJob?.cancel()
        streamJob = null
        viewModelScope.launch {
            ensureSession(text)
            val sid = currentSessionId; if (sid.isEmpty()) return@launch

            // Read all attached files
            val imageParts = fileUris.mapNotNull { readFileForUpload(it) }
            var userContent = text.trim()
            if (imageParts.isNotEmpty() && userContent.isBlank()) userContent = "请描述这些图片"

            // Non-vision chat models can't handle images — warn and strip
            val isImageModel = _activeModel.value?.category == ModelCategory.IMAGE
            val effectiveParts = if (!isImageModel && imageParts.isNotEmpty()) {
                val warning = Message(id = java.util.UUID.randomUUID().toString(), sessionId = sid,
                    role = MessageRole.ASSISTANT, content = "⚠️ 当前模型不支持图片识别，图片未发送。\n请切换到支持视觉的模型后重试。",
                    timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
                InMemoryStore.insertMessage(warning)
                emptyList()
            } else imageParts

            // Collect all uploaded image URIs for the user bubble
            val allImageUris = fileUris.map { it.toString() }
            // For image models with files: pass first file's base64 for legacy compat
            val firstBase64 = effectiveParts.firstOrNull()?.let { p ->
                android.util.Base64.encodeToString(p.bytes, android.util.Base64.NO_WRAP)
            }
            val firstMime = effectiveParts.firstOrNull()?.mimeType

            val um = Message(
                id = UUID.randomUUID().toString(), sessionId = sid,
                role = MessageRole.USER, content = userContent,
                imageUrls = allImageUris,
                timestamp = System.currentTimeMillis(), status = MessageStatus.DONE
            )
            InMemoryStore.insertMessage(um)
            InMemoryStore.updateSessionActivity(sid, System.currentTimeMillis())

            val model = _activeModel.value
            val config = model?.let { keyStore.loadPlatforms().find { p -> p.id == it.platformId } }

            if (model?.category == ModelCategory.IMAGE) {
                generateImageFlow(sid, userContent, model, config, effectiveParts, editImage)
            } else {
                startStreamingChat(sid, userContent, config, model, firstBase64, firstMime)
            }
        }
    }

    private fun readFileForUpload(uri: android.net.Uri): ImagePart? {
        return try {
            val cr = getApplication<android.app.Application>().contentResolver
            val mimeType = cr.getType(uri) ?: kotlin.run {
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
            ImagePart(bytes, mimeType)
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
                    withTimeout(300_000) {
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
                    } // withTimeout
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

    private fun finishGenerating(rm: Message, sid: String, urls: List<String>?, error: String?) {
        _genHint.value = ""
        hintJob?.cancel(); hintJob = null
        imageGenJob = null
        val final = if (error != null) {
            rm.copy(content = "❌ $error", status = MessageStatus.DONE, contentType = ContentType.ERROR)
        } else {
            rm.copy(content = "生成图片", imageUrls = urls ?: emptyList(),
                status = MessageStatus.DONE, contentType = ContentType.IMAGE)
        }
        // Always persist
        InMemoryStore.insertMessage(final)
        // Only update UI if still on the same session
        if (sid == currentSessionId) {
            _messages.update { list -> list.map { if (it.id == rm.id) final else it } }
        }
    }

    // ---- image generation ----

    private fun generateImageFlow(
        sid: String, prompt: String,
        model: ModelConfig, config: com.aiaggregator.app.data.model.ApiConfig?,
        attachedParts: List<ImagePart> = emptyList(),
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
            contentType = ContentType.IMAGE, modelName = model.displayName.ifBlank { model.modelName })
        InMemoryStore.insertMessage(rm)

        // Start rotating hints in independent scope
        hintJob?.cancel()
        hintJob = imageGenScope.launch {
            var idx = 0
            while (true) {
                _genHint.value = genHints[idx % genHints.size]
                idx++
                kotlinx.coroutines.delay(3500)
            }
        }
        imageGenJob = imageGenScope.launch {
            try {
                // Collect all reference images
                val refImages = mutableListOf<ImagePart>()
                refImages.addAll(attachedParts)

                // If edit toggle is ON and there's a previous AI image, download and add it
                if (editImage) {
                    val lastImg = _messages.value.lastOrNull { it.role == MessageRole.ASSISTANT && it.allImageUrls.isNotEmpty() }
                    if (lastImg != null) {
                        val lastUrl = lastImg.allImageUrls.first()
                        appendContent(rm, "正在下载上一张图片...\n")
                        val prevBytes = withContext(Dispatchers.IO) { downloadImage(lastUrl) }
                        if (prevBytes != null) {
                            val prevMime = guessMimeFromUrl(lastUrl)
                            refImages.add(ImagePart(prevBytes, prevMime))
                        }
                    }
                }

                val result = if (refImages.isNotEmpty()) {
                    appendContent(rm, "正在根据${refImages.size}张参考图生成...\n")
                    withContext(Dispatchers.IO) {
                        chatService.editImage(prompt, model.modelName, refImages, config)
                    }
                } else {
                    withContext(Dispatchers.IO) { chatService.generateImage(prompt, model.modelName, config) }
                }

                if (result.error != null) {
                    finishGenerating(rm, sid, null, result.error)
                } else {
                    val displayUrl = resolveDisplayUrl(result)
                    if (displayUrl == null) {
                        finishGenerating(rm, sid, null, "未返回图片")
                    } else {
                        finishGenerating(rm, sid, listOf(displayUrl), null)
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Silently stop — user switched away, generation result discarded
                _genHint.value = ""
                hintJob?.cancel(); hintJob = null
                imageGenJob = null
            } catch (e: Exception) {
                val detail = when {
                    e is java.io.IOException -> "网络连接失败"
                    else -> e.message ?: "未知错误"
                }
                finishGenerating(rm, sid, null, "图片处理失败: $detail")
            }
        }
    }

    private fun resolveDisplayUrl(result: ImageGenResult): String? {
        // Prefer URL if available
        result.urls.firstOrNull()?.let { return it }
        // Decode first b64_json to cache file
        result.base64Images.firstOrNull()?.let { b64 ->
            return try {
                val bytes = android.util.Base64.decode(b64, android.util.Base64.NO_WRAP)
                val cacheDir = java.io.File(getApplication<android.app.Application>().cacheDir, "images")
                cacheDir.mkdirs()
                val file = java.io.File(cacheDir, "gen_${System.currentTimeMillis()}.png")
                file.writeBytes(bytes)
                file.toURI().toString()
            } catch (_: Exception) { null }
        }
        return null
    }

    private fun guessMimeFromUrl(url: String): String {
        return when {
            url.contains(".png") || url.contains("image/png") -> "image/png"
            url.contains(".webp") || url.contains("image/webp") -> "image/webp"
            url.contains(".jpg") || url.contains(".jpeg") || url.contains("image/jpeg") -> "image/jpeg"
            else -> "image/png"
        }
    }

    private suspend fun downloadImage(url: String): ByteArray? {
        // If it's a local file:// URI, read directly
        if (url.startsWith("file://")) {
            return try {
                java.io.File(java.net.URI(url)).readBytes()
            } catch (_: Exception) { null }
        }
        return try {
            val client = com.aiaggregator.app.data.remote.HttpClientFactory.client
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
        // No delay — sessionCollectorJob guarantees sequential startup
    }
}
