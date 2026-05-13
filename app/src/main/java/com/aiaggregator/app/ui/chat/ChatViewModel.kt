package com.aiaggregator.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiaggregator.app.business.adapter.ChatMessageItem
import com.aiaggregator.app.business.adapter.ImagePart
import com.aiaggregator.app.business.adapter.ImageGenResult
import com.aiaggregator.app.business.chat.ChatService
import com.aiaggregator.app.data.local.ApiKeyStore
import com.aiaggregator.app.data.local.ImageStorageManager
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

	private companion object {
		private const val RICH_MARKDOWN_SYSTEM_PROMPT = """
你是清畅AI中的助手。以下规则只控制回答的呈现方式，不改变用户请求本身。

总原则：
1. 默认使用简洁、自然、适合移动端阅读的 Markdown；只有在能明显提升理解、对比、排错或操作效率时，才使用表格、提示块、折叠等增强格式。
2. 临时格式要求优先：如果用户明确指定“只输出 JSON / 不要 Markdown / 固定格式 / 表格 / 原文”等格式，按用户当前要求执行。
3. 不确定是否需要复杂格式时，使用普通文本或普通列表，不要为了样式而样式化。
4. 不要输出 HTML、CSS、JavaScript、XML，除非用户明确要求该格式；折叠内容只在确有必要时使用 details/summary。

原文保护：
1. 用户提供的代码、命令、配置、日志、报错、引用文本、待润色原文，必须保持语义和文本完整性。
2. 当用户要求“原文/不要改/逐字/只排版/保留格式”时，不得擅自纠错、翻译、补全、删改、重排语义或替换链接文案。
3. 如需解释，只在原文外部追加说明。

格式触发规则：
1. 表格仅用于三项以上对象横向对比、字段明确的参数/价格/版本/配置，或用户明确要求表格；两三点说明优先用列表。
2. 提示块仅用于真实错误、重要风险、关键限制、验证完成或必要补充；普通说明不要套 info，普通建议不要套 warning，轻微问题不要套 error。
3. 折叠仅用于较长且非核心的日志、配置、参考材料；核心结论、操作步骤、错误原因不要默认折叠。
4. 代码、命令、JSON、配置、日志、正则表达式使用带语言名的 fenced code block。
5. 链接优先使用有意义的 Markdown 命名链接；但用户要求保留原文时不要改写裸链。

禁止过度格式化：
1. 不要因为出现数字就强制表格。
2. 不要给短回答强行加标题、分隔线、提示块或折叠。
3. 不要无条件加粗重点、重写标题层级或插入装饰性符号。
4. 不要主动添加图片，除非用户要求或图片 URL 来源可靠且与任务直接相关。
"""
	}

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
        // Don't clear _genHint here: image generation may continue in another session.
        // The UI uses each message timestamp as the authoritative elapsed timer.
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
        viewModelScope.launch {
            val sid = currentSessionId
            if (sid.isBlank()) return@launch
            val snapshot = _messages.value
            val assistantIndex = snapshot.indexOfLast { it.role == MessageRole.ASSISTANT }
            val sourceUser = if (assistantIndex >= 0) {
                snapshot.take(assistantIndex).lastOrNull { it.role == MessageRole.USER }
            } else {
                snapshot.lastOrNull { it.role == MessageRole.USER }
            }
            if (sourceUser == null) {
                InMemoryStore.insertMessage(
                    Message(
                        id = UUID.randomUUID().toString(), sessionId = sid,
                        role = MessageRole.ASSISTANT,
                        content = "⚠️ 找不到可重新生成的上一条用户输入。",
                        contentType = ContentType.ERROR,
                        timestamp = System.currentTimeMillis(), status = MessageStatus.DONE
                    ),
                    saveImmediately = true
                )
                return@launch
            }

            if (assistantIndex >= 0) {
                InMemoryStore.deleteMessage(snapshot[assistantIndex].id)
            }

            lastUserPrompt = sourceUser.content
            lastUserImageData = null
            val model = _activeModel.value
            val config = model?.let { keyStore.loadPlatforms().find { p -> p.id == it.platformId } }
            val imageParts = withContext(Dispatchers.IO) {
                sourceUser.allImageUrls.mapNotNull { url ->
                    runCatching { readFileForUpload(android.net.Uri.parse(url)) }.getOrNull()
                }
            }
            val chatImages = imageParts.map { p ->
                com.aiaggregator.app.business.adapter.ChatImage(
                    base64 = android.util.Base64.encodeToString(p.bytes, android.util.Base64.NO_WRAP),
                    mimeType = p.mimeType
                )
            }
            if (model?.category == ModelCategory.IMAGE) {
                generateImageFlow(sid, sourceUser.content.ifBlank { "请描述这些图片" }, model, config, imageParts, editImage = false)
            } else {
                startStreamingChat(
                    sid = sid,
                    userText = sourceUser.content.ifBlank { "请描述这些图片" },
                    config = config,
                    model = model,
                    images = chatImages,
                    currentUserMessageId = sourceUser.id
                )
            }
        }
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

            // Check storage space
            val freeMB = withContext(Dispatchers.IO) {
                getApplication<android.app.Application>().filesDir.usableSpace / (1024 * 1024)
            }
            if (freeMB < 100) {
                val warning = Message(id = java.util.UUID.randomUUID().toString(), sessionId = sid,
                    role = MessageRole.ASSISTANT,
                    content = "⚠️ 手机存储空间不足（剩余 ${freeMB}MB），可能影响图片保存和聊天记录。建议清理后再使用。",
                    timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
                InMemoryStore.insertMessage(warning)
            }

            // Read all attached files on IO thread, save to persistent storage
            val imageParts = mutableListOf<ImagePart>()
            val displayUris = mutableListOf<String>()
            val oversizedUris = mutableListOf<String>()
            withContext(Dispatchers.IO) {
                val cr = getApplication<android.app.Application>().contentResolver
                val imgDir = java.io.File(getApplication<android.app.Application>().filesDir, "images")
                imgDir.mkdirs()
                for (uri in fileUris) {
                    val fileSize = try {
                        cr.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
                    } catch (_: Exception) { -1L }
                    if (fileSize > 50 * 1024 * 1024) {
                        oversizedUris.add(uri.toString())
                        android.util.Log.w("AIAPP", "Skip oversized image before read: $uri (${fileSize} bytes)")
                        continue
                    }
                    val part = readFileForUpload(uri) ?: continue
                    if (part.bytes.size > 50 * 1024 * 1024) {
                        oversizedUris.add(uri.toString())
                        continue
                    }
                    imageParts.add(part)
                    // Save to persistent storage so old messages keep their images
                    val ext = when {
                        part.mimeType.contains("png") -> "png"
                        part.mimeType.contains("webp") -> "webp"
                        part.mimeType.contains("gif") -> "gif"
                        else -> "jpg"
                    }
                    val file = java.io.File(imgDir, "img_${System.currentTimeMillis()}_${imageParts.size}.$ext")
                    file.writeBytes(part.bytes)
                    displayUris.add(file.toURI().toString())
                }
            }
            if (oversizedUris.isNotEmpty()) {
                val warning = Message(id = java.util.UUID.randomUUID().toString(), sessionId = sid,
                    role = MessageRole.ASSISTANT,
                    content = "⚠️ ${oversizedUris.size} 张图片超过 50MB 限制（OpenAI 官方上限），已自动跳过。建议先压缩再上传。",
                    timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
                InMemoryStore.insertMessage(warning)
            }
            var userContent = text.trim()
            if (imageParts.isNotEmpty() && userContent.isBlank()) userContent = "请描述这些图片"

            // Non-vision chat models can't handle images — warn and strip
            val supportsVision = _activeModel.value?.category in setOf(ModelCategory.IMAGE, ModelCategory.MULTIMODAL)
            val effectiveParts = if (!supportsVision && imageParts.isNotEmpty()) {
                val warning = Message(id = java.util.UUID.randomUUID().toString(), sessionId = sid,
                    role = MessageRole.ASSISTANT, content = "⚠️ 当前模型不支持图片识别，图片未发送。\n请切换到支持视觉的模型后重试。",
                    timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
                InMemoryStore.insertMessage(warning)
                emptyList()
            } else imageParts

            // Store persistent file:// URIs — survive app restart
            val allImageUris = displayUris
            val chatImages = effectiveParts.map { p ->
                com.aiaggregator.app.business.adapter.ChatImage(
                    base64 = android.util.Base64.encodeToString(p.bytes, android.util.Base64.NO_WRAP),
                    mimeType = p.mimeType
                )
            }

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
                startStreamingChat(sid, userContent, config, model, chatImages, um.id)
            }
        }
    }

    private fun readFileForUpload(uri: android.net.Uri): ImagePart? {
        try {
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
            val maxBytes = 50L * 1024L * 1024L
            val maxUploadEdge = 3072
            // Three fallback methods — cropped images may need #2 or #3
            var bytes: ByteArray? = null
            // Method 1: openInputStream with hard byte limit
            try {
                cr.openInputStream(uri)?.use { input ->
                    bytes = readLimitedBytes(input, maxBytes)
                }
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("AIAPP", "Skip oversized image stream: $uri (${e.message})")
                return null
            } catch (_: Exception) {}
            // Method 2: openFileDescriptor with sampled bitmap fallback
            if (bytes == null) {
                try {
                    cr.openFileDescriptor(uri, "r")?.use { fd ->
                        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        android.graphics.BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, bounds)
                        val opts = android.graphics.BitmapFactory.Options().apply {
                            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxUploadEdge, maxUploadEdge)
                        }
                        val bmp = android.graphics.BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, opts)
                        if (bmp != null) {
                            java.io.ByteArrayOutputStream().use { stream ->
                                bmp.compress(if (mimeType.contains("png")) android.graphics.Bitmap.CompressFormat.PNG else android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
                                bytes = stream.toByteArray().also {
                                    if (it.size > maxBytes) throw IllegalArgumentException("compressed image exceeds ${maxBytes} bytes")
                                }
                            }
                            bmp.recycle()
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    android.util.Log.w("AIAPP", "Skip oversized decoded image: $uri (${e.message})")
                    return null
                } catch (e: OutOfMemoryError) {
                    android.util.Log.e("AIAPP", "Image decode OOM: $uri", e)
                    return null
                } catch (_: Exception) {}
            }
            // Method 3: openAssetFileDescriptor with hard byte limit (some gallery apps only support this)
            if (bytes == null) {
                try {
                    cr.openAssetFileDescriptor(uri, "r")?.use { afd ->
                        bytes = afd.createInputStream().use { readLimitedBytes(it, maxBytes) }
                    }
                } catch (e: IllegalArgumentException) {
                    android.util.Log.w("AIAPP", "Skip oversized asset image: $uri (${e.message})")
                    return null
                } catch (_: Exception) {}
            }
            if (bytes == null) {
                android.util.Log.e("AIAPP", "All methods failed for: $uri (mime=$mimeType)")
                return null
            }
            return ImagePart(bytes, mimeType)
        } catch (e: Exception) { android.util.Log.e("AIAPP", "readFile err: ${e.message}"); return null }
    }

    private fun readLimitedBytes(input: java.io.InputStream, maxBytes: Long): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            total += read
            if (total > maxBytes) throw IllegalArgumentException("image exceeds ${maxBytes} bytes")
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var inSampleSize = 1
        var halfWidth = width / 2
        var halfHeight = height / 2
        while ((halfWidth / inSampleSize) >= reqWidth || (halfHeight / inSampleSize) >= reqHeight) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    // ---- streaming chat ----

    private fun startStreamingChat(
        sid: String, userText: String,
        config: com.aiaggregator.app.data.model.ApiConfig?, model: ModelConfig?,
        images: List<com.aiaggregator.app.business.adapter.ChatImage> = emptyList(),
        currentUserMessageId: String
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
            val deltaBuffer = StringBuilder()
            var lastFlushAt = 0L
            fun flushBufferedContent() {
                if (deltaBuffer.isEmpty()) return
                val text = deltaBuffer.toString()
                deltaBuffer.clear()
                _messages.update { list ->
                    list.map { if (it.id == rm.id) it.copy(content = it.content + text) else it }
                }
                lastFlushAt = System.currentTimeMillis()
            }
            fun appendBufferedContent(delta: String) {
                deltaBuffer.append(delta)
                val now = System.currentTimeMillis()
                if (now - lastFlushAt >= 50L || deltaBuffer.length >= 512) {
                    flushBufferedContent()
                }
            }

            try {
                val conversationMessages = _messages.value
                    .filter { it.role != MessageRole.SYSTEM && it.id != rm.id && it.id != currentUserMessageId }
                    .let { if (it.size > 40) it.takeLast(40) else it }
                    .map { ChatMessageItem(it.role.name.lowercase(), it.content) } +
                    ChatMessageItem("user", userText, images = images)
                val history = listOf(ChatMessageItem("system", RICH_MARKDOWN_SYSTEM_PROMPT.trim())) + conversationMessages
                val cfg = config.copy()

                withContext(Dispatchers.IO) {
                    withTimeout(300_000) {
                        chatService.streamChat(history, cfg, model.modelName).collect { chunk ->
                            when {
                                chunk.error != null -> {
                                    flushBufferedContent()
                                    finishWithError(rm, chunk.error)
                                }
                                chunk.isDone -> {
                                    flushBufferedContent()
                                    if (chunk.usage != null) {
                                        _messages.update { list -> list.map { if (it.id == rm.id) it.copy(promptTokens = chunk.usage!!.promptTokens, completionTokens = chunk.usage!!.completionTokens, tokenCount = chunk.usage!!.totalTokens) else it } }
                                    }
                                    finishStreaming(rm)
                                }
                                chunk.content != null -> appendBufferedContent(chunk.content!!)
                            }
                        }
                    } // withTimeout
                }
            } catch (e: CancellationException) {
                flushBufferedContent()
                finishStreaming(rm)
            } catch (e: UnknownHostException) {
                flushBufferedContent()
                finishWithError(rm, "无法连接: ${e.message}")
            } catch (e: ConnectException) {
                flushBufferedContent()
                finishWithError(rm, "连接被拒绝")
            } catch (e: SocketTimeoutException) {
                flushBufferedContent()
                finishWithError(rm, "请求超时")
            } catch (e: java.io.IOException) {
                flushBufferedContent()
                finishWithError(rm, "网络异常: ${e.message}")
            } catch (e: Exception) {
                flushBufferedContent()
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
        val current = _messages.value.find { it.id == rm.id } ?: rm
        val final = if (current.content.isBlank()) {
            current.copy(
                content = "⚠️ 未收到模型返回内容。可能是模型只返回了工具调用/思考内容、联网搜索结果未透传，或中转站过滤了正文。请点击重新生成或切换模型/平台后重试。",
                status = MessageStatus.DONE,
                contentType = ContentType.ERROR
            )
        } else {
            current.copy(status = MessageStatus.DONE)
        }
        _messages.update { list ->
            list.map { if (it.id == rm.id) final else it }
        }
        InMemoryStore.insertMessage(final, saveImmediately = true)
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
        val final = _messages.value.find { it.id == rm.id } ?: rm.copy(content = display, status = MessageStatus.DONE, contentType = ContentType.ERROR)
        InMemoryStore.insertMessage(final, saveImmediately = true)
    }

    private fun finishGenerating(rm: Message, sid: String, urls: List<String>?, error: String?) {
        _genHint.value = ""
        hintJob?.cancel(); hintJob = null
        imageGenJob = null
        val final = if (error != null && urls.isNullOrEmpty()) {
            rm.copy(content = "❌ $error", status = MessageStatus.DONE, contentType = ContentType.ERROR)
        } else {
            rm.copy(
                content = error?.let { "生成图片\n\n⚠️ $it" } ?: "生成图片",
                imageUrls = urls ?: emptyList(),
                status = MessageStatus.DONE,
                contentType = ContentType.IMAGE
            )
        }
        persistGeneratedMessage(final, sid)
    }

    private fun finishGenerationCancelled(rm: Message, sid: String) {
        _genHint.value = ""
        hintJob?.cancel(); hintJob = null
        imageGenJob = null
        val final = rm.copy(content = "已停止生成图片", status = MessageStatus.DONE, contentType = ContentType.TEXT)
        persistGeneratedMessage(final, sid)
    }

    private fun persistGeneratedMessage(final: Message, sid: String) {
        InMemoryStore.insertMessage(final, saveImmediately = true)
        if (sid == currentSessionId) {
            _messages.update { list -> list.map { if (it.id == final.id) final else it } }
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
        if (imageGenJob?.isActive == true) {
            val rm = Message(id = UUID.randomUUID().toString(), sessionId = sid, role = MessageRole.ASSISTANT,
                content = "⚠️ 正在生成图片，请先等待完成或点击停止后再发送新的图片生成任务。",
                timestamp = System.currentTimeMillis(), status = MessageStatus.DONE, contentType = ContentType.ERROR)
            InMemoryStore.insertMessage(rm, saveImmediately = true)
            return
        }

        val startedAt = System.currentTimeMillis()
        val rm = Message(id = UUID.randomUUID().toString(), sessionId = sid, role = MessageRole.ASSISTANT,
            content = "", timestamp = startedAt, status = MessageStatus.STREAMING,
            contentType = ContentType.IMAGE, modelName = model.displayName.ifBlank { model.modelName },
            metadata = "type=image_generation;startedAt=$startedAt;prompt=${prompt.take(120)};model=${model.modelName}")
        InMemoryStore.insertMessage(rm, saveImmediately = true)

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
                    val imageResult = resolveDisplayUrl(result)
                    if (imageResult.url == null) {
                        finishGenerating(rm, sid, null, imageResult.cacheWarning ?: "未返回图片")
                    } else {
                        finishGenerating(rm, sid, listOf(imageResult.url), imageResult.cacheWarning)
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                _genHint.value = ""
                hintJob?.cancel(); hintJob = null
                if (imageGenJob === coroutineContext[Job]) imageGenJob = null
                finishGenerationCancelled(rm, sid)
            } catch (e: Exception) {
                val detail = when {
                    e is java.io.IOException -> "网络连接失败"
                    else -> e.message ?: "未知错误"
                }
                finishGenerating(rm, sid, null, "图片处理失败: $detail")
            }
        }
    }

    private data class ResolvedImageResult(
        val url: String?,
        val cacheWarning: String? = null
    )

    private fun resolveDisplayUrl(result: ImageGenResult): ResolvedImageResult {
        val app = getApplication<android.app.Application>()
        result.urls.firstOrNull()?.let { url ->
            ImageStorageManager.persistImageUrl(app, url)?.let { return ResolvedImageResult(it) }
            return ResolvedImageResult(url, "图片已生成，但本地缓存失败；当前先显示远程图片，分享或离线查看可能受影响。")
        }
        result.base64Images.firstOrNull()?.let { b64 ->
            val local = ImageStorageManager.persistBase64Image(app, b64)
            return ResolvedImageResult(local, if (local == null) "图片已返回，但本地保存失败。" else null)
        }
        return ResolvedImageResult(null)
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
        return withContext(Dispatchers.IO) {
            ImageStorageManager.readImageBytes(getApplication<android.app.Application>(), url)
        }
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
