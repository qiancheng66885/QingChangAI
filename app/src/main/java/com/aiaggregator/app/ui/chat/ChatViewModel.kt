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
import kotlinx.coroutines.TimeoutCancellationException
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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

	private companion object {
		private const val MAX_UPLOAD_IMAGE_BYTES = 5L * 1024L * 1024L
		private const val TARGET_UPLOAD_IMAGE_BYTES = 3L * 1024L * 1024L
		private const val MAX_UPLOAD_TOTAL_BYTES = 30L * 1024L * 1024L
		private const val MAX_UPLOAD_IMAGE_EDGE = 1600
		private const val CONTENT_STREAM_FLUSH_INTERVAL_MS = 120L
		private const val REASONING_STREAM_FLUSH_INTERVAL_MS = 420L
		private const val CONTENT_STREAM_FLUSH_CHARS = 1024
		private const val REASONING_STREAM_FLUSH_CHARS = 1800
		private const val STREAM_IDLE_WARNING_MS = 25_000L
		private const val STREAM_IDLE_CHECK_MS = 5_000L
		private const val MAX_CONTEXT_MESSAGES = 24
		private const val MAX_CONTEXT_CHARS = 60_000
		private const val MAX_MESSAGE_CONTEXT_CHARS = 8_000
		private const val MAX_USER_PROMPT_CHARS = 12_000
		private const val MAX_CHAT_IMAGES = 9
		private const val JPEG_UPLOAD_QUALITY = 82
		private const val PNG_UPLOAD_QUALITY = 90
		private const val DEEP_THINK_REASONING_EFFORT = "high"
		private const val DEEP_THINK_BUDGET_TOKENS = "1024"
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
		private const val DEEP_THINK_SYSTEM_PROMPT = """
深度思考已开启：
1. 回答前请更充分地分析问题，优先保证准确性、完整性和自洽性。
2. 不要为了展示过程而把隐藏链式思考强行写进最终答案；最终答案仍然保持清晰、直接、可读。
3. 如果当前模型或中转站支持独立的 reasoning / thinking 字段，客户端会把它展示为“深度思考”。
4. 如果接口不支持独立思考字段，只输出高质量最终答案即可。
5. 如果模型会输出可见的思考摘要、推理摘要或 `<think>` 内容，优先使用用户提问的语言；中文用户默认使用中文。
6. 最终答案禁止出现“思考过程”“推理摘要”“可读推理摘要”“reasoning summary”“chain of thought”等标题；这些内容属于客户端的“深度思考”区域，最终答案只写结论、分析和可执行建议。
"""
        private const val FINAL_FROM_REASONING_SYSTEM_PROMPT = """
你是清畅AI中的助手。上一次模型或中转站只返回了可见思考摘要，没有返回最终答案。
现在请根据用户原始问题和已收到的思考摘要，补写最终答案。
规则：
1. 只输出最终答案，不要继续输出思考过程。
2. 禁止出现“思考过程”“推理摘要”“可读推理摘要”“reasoning summary”“chain of thought”等标题。
3. 如果思考摘要显示原题无解或存在矛盾，请直接给出结论和简短核验。
4. 如果信息不足，说明缺少什么，不要编造。
"""
        private const val REASONING_ONLY_FALLBACK_TEXT =
            "已收到模型的思考过程，但这次中转站没有返回最终答案。可以点下方“补出答案”，让模型只根据当前问题和已收到的思考摘要补写正文。"
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
    private var lastDeepThink: Boolean = false

    val currentSessionTitle: String? get() = sessions.value.find { it.id == currentSessionId }?.title

    private val _activeModel = MutableStateFlow<ModelConfig?>(null)
    val activeModel: StateFlow<ModelConfig?> = _activeModel.asStateFlow()

    private val _availableModels = MutableStateFlow<List<ModelConfig>>(emptyList())
    val availableModelsState: StateFlow<List<ModelConfig>> = _availableModels.asStateFlow()
    val availableModels: List<ModelConfig> get() = _availableModels.value

    private val _platforms = MutableStateFlow<List<com.aiaggregator.app.data.model.ApiConfig>>(emptyList())

    private var streamJob: Job? = null
    private var sessionCollectorJob: Job? = null
    private var imageGenJob: Job? = null
    private var hintJob: Job? = null

    /** 图片生成状态文本 — UI 观察它来显示轮播提示 */
    private val _genHint = MutableStateFlow("")
    val genHint: StateFlow<String> = _genHint.asStateFlow()

    private val genHints = listOf(
        "正在等待图片模型返回结果...",
        "不同平台和中转站速度会有差异",
        "复杂图片、参考图越多通常越慢",
        "2 分钟左右甚至更久属于正常现象",
        "请尽量保持 App 在前台和网络稳定",
        "仍在等待上游返回，可以继续等待或停止重试"
    )

    fun switchModel(model: ModelConfig) {
        _activeModel.value = model
    }

    fun refreshConfig() {
        viewModelScope.launch {
            val (models, platforms) = withContext(Dispatchers.IO) {
                keyStore.loadModels() to keyStore.loadPlatforms()
            }
            val normalizedModels = models.normalizedPickerSelection()
            val visibleModels = normalizedModels.filter { it.showInPicker }
            _availableModels.value = visibleModels
            _platforms.value = platforms
            _activeModel.value = resolveActiveModel(normalizedModels, visibleModels)
        }
    }

    private suspend fun platformFor(model: ModelConfig?): com.aiaggregator.app.data.model.ApiConfig? {
        if (model == null) return null
        _platforms.value.find { it.id == model.platformId }?.let { return it }
        refreshConfigNow()
        return _platforms.value.find { it.id == model.platformId }
    }

    private suspend fun refreshConfigNow() {
        val (models, platforms) = withContext(Dispatchers.IO) {
            keyStore.loadModels() to keyStore.loadPlatforms()
        }
        val normalizedModels = models.normalizedPickerSelection()
        val visibleModels = normalizedModels.filter { it.showInPicker }
        _availableModels.value = visibleModels
        _platforms.value = platforms
        _activeModel.value = resolveActiveModel(normalizedModels, visibleModels)
    }

    private fun resolveActiveModel(
        models: List<ModelConfig>,
        visibleModels: List<ModelConfig>
    ): ModelConfig? {
        val currentId = _activeModel.value?.id
        val current = visibleModels.find { it.id == currentId }
        if (current != null) return current
        return models.find { it.isDefault && it.category != ModelCategory.IMAGE }
            ?: visibleModels.firstOrNull { it.category != ModelCategory.IMAGE }
            ?: visibleModels.firstOrNull()
            ?: models.firstOrNull { it.category != ModelCategory.IMAGE }
            ?: models.firstOrNull()
    }

    private fun List<ModelConfig>.normalizedPickerSelection(): List<ModelConfig> {
        val defaultId = firstOrNull { it.isDefault && it.category != ModelCategory.IMAGE }?.id
        val selectedIds = groupBy { it.category }.values.mapNotNull { group ->
            group.firstOrNull { it.showInPicker }?.id ?: group.firstOrNull()?.id
        }.toSet()
        return map {
            it.copy(
                isDefault = it.id == defaultId,
                showInPicker = it.id in selectedIds
            )
        }
    }

    init {
        refreshConfig()
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

    fun renameSession(sid: String, title: String) {
        val cleaned = title.trim()
        if (sid.isBlank() || cleaned.isBlank()) return
        viewModelScope.launch {
            InMemoryStore.updateSessionTitle(sid, cleaned.take(80))
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

            val regenerateDeepThink = assistantIndex >= 0 &&
                snapshot[assistantIndex].metadata?.contains("deepThink=true") == true
            if (assistantIndex >= 0) {
                InMemoryStore.deleteMessage(snapshot[assistantIndex].id)
            }

            lastUserPrompt = sourceUser.content
            lastUserImageData = null
            lastDeepThink = regenerateDeepThink
            val model = _activeModel.value
            val config = platformFor(model)
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
                    currentUserMessageId = sourceUser.id,
                    deepThink = regenerateDeepThink
                )
            }
        }
    }

    fun completeReasoningOnly(assistantMessageId: String) {
        viewModelScope.launch {
            val sid = currentSessionId
            if (sid.isBlank()) return@launch
            val snapshot = _messages.value
            val assistantIndex = snapshot.indexOfFirst { it.id == assistantMessageId }
            val target = snapshot.getOrNull(assistantIndex) ?: return@launch
            val reasoning = target.reasoningContent?.takeIf { it.isNotBlank() } ?: return@launch
            val sourceUser = snapshot.take(assistantIndex).lastOrNull { it.role == MessageRole.USER }
            if (sourceUser == null) {
                val failed = target.copy(
                    content = "⚠️ 找不到这次思考对应的用户问题，无法补出最终答案。",
                    status = MessageStatus.DONE,
                    contentType = ContentType.ERROR
                )
                _messages.update { list -> list.map { if (it.id == target.id) failed else it } }
                InMemoryStore.insertMessage(failed, saveImmediately = true)
                return@launch
            }

            val model = _activeModel.value
            val config = platformFor(model)
            if (model == null || config == null || model.category == ModelCategory.IMAGE ||
                config.baseUrl.isBlank() || config.apiKey.isBlank() || model.modelName.isBlank()
            ) {
                val failed = target.copy(
                    content = "⚠️ 当前模型或平台配置不完整，无法根据思考补出最终答案。请切换到聊天模型后重试。",
                    status = MessageStatus.DONE,
                    contentType = ContentType.ERROR
                )
                _messages.update { list -> list.map { if (it.id == target.id) failed else it } }
                InMemoryStore.insertMessage(failed, saveImmediately = true)
                return@launch
            }

            streamJob?.cancel()
            val streamingTarget = target.copy(
                content = "",
                status = MessageStatus.STREAMING,
                contentType = ContentType.TEXT,
                metadata = appendMetadataFlag(target.metadata, "reasoningOnlyCompletion=true")
            )
            _messages.update { list -> list.map { if (it.id == target.id) streamingTarget else it } }
            InMemoryStore.insertMessage(streamingTarget)

            streamJob = viewModelScope.launch {
                streamFinalAnswerFromReasoning(
                    target = streamingTarget,
                    sourceUser = sourceUser,
                    reasoning = reasoning,
                    config = config,
                    model = model
                )
            }
        }
    }

    fun sendMessage(text: String, fileUris: List<android.net.Uri> = emptyList(), editImage: Boolean = false, deepThink: Boolean = false) {
        if (text.isBlank() && fileUris.isEmpty()) return
        lastUserPrompt = text
        lastUserImageData = Pair(fileUris, editImage)
        lastDeepThink = deepThink
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
            val availableMemoryMb = availableMemoryMb()
            if (availableMemoryMb < 96) {
                val warning = Message(id = java.util.UUID.randomUUID().toString(), sessionId = sid,
                    role = MessageRole.ASSISTANT,
                    content = "⚠️ 当前可用运行内存偏低（约 ${availableMemoryMb}MB），多图或长回答可能变慢。建议先关闭后台应用或分批发送图片。",
                    timestamp = System.currentTimeMillis(), status = MessageStatus.DONE)
                InMemoryStore.insertMessage(warning)
            }

            // Read all attached files on IO thread, save to persistent storage
            val imageParts = mutableListOf<ImagePart>()
            val displayUris = mutableListOf<String>()
            val oversizedUris = mutableListOf<String>()
            withContext(Dispatchers.IO) {
                val app = getApplication<android.app.Application>()
                val cr = app.contentResolver
                var totalUploadBytes = 0L
                android.util.Log.i("AIAPP", "sendMessage images=${fileUris.size}, messages=${_messages.value.size}, messagesJson=${messagesJsonSizeBytes()} bytes, availMem=${availableMemoryMb()}MB")
                for (uri in fileUris.take(MAX_CHAT_IMAGES)) {
                    val fileSize = try {
                        cr.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
                    } catch (_: Exception) { -1L }
                    if (fileSize > 80 * 1024 * 1024) {
                        oversizedUris.add(uri.toString())
                        android.util.Log.w("AIAPP", "Skip oversized image before compress: $uri (${fileSize} bytes)")
                        continue
                    }
                    val part = readFileForUpload(uri) ?: run {
                        oversizedUris.add(uri.toString())
                        continue
                    }
                    android.util.Log.i("AIAPP", "upload image compressed: uri=$uri original=$fileSize compressed=${part.bytes.size} mime=${part.mimeType}")
                    if (part.bytes.size > MAX_UPLOAD_IMAGE_BYTES || totalUploadBytes + part.bytes.size > MAX_UPLOAD_TOTAL_BYTES) {
                        oversizedUris.add(uri.toString())
                        android.util.Log.w("AIAPP", "Skip compressed image over limit: $uri (${part.bytes.size} bytes, total=$totalUploadBytes)")
                        continue
                    }
                    totalUploadBytes += part.bytes.size
                    imageParts.add(part)
                    persistUploadImage(part)?.let { displayUris.add(it) }
                }
                if (fileUris.size > MAX_CHAT_IMAGES) {
                    oversizedUris.addAll(fileUris.drop(MAX_CHAT_IMAGES).map { it.toString() })
                }
            }
            if (oversizedUris.isNotEmpty()) {
                val warning = Message(id = java.util.UUID.randomUUID().toString(), sessionId = sid,
                    role = MessageRole.ASSISTANT,
                    content = "⚠️ ${oversizedUris.size} 张图片因数量、压缩失败或体积超限已自动跳过。建议分批发送，单张压缩后不超过 5MB。",
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
            val config = platformFor(model)

            if (model?.category == ModelCategory.IMAGE) {
                generateImageFlow(sid, userContent, model, config, effectiveParts, editImage)
            } else {
                startStreamingChat(sid, userContent, config, model, chatImages, um.id, deepThink)
            }
        }
    }

    private fun readFileForUpload(uri: android.net.Uri): ImagePart? {
        val cr = getApplication<android.app.Application>().contentResolver
        val sourceMime = cr.getType(uri) ?: kotlin.run {
            val path = uri.lastPathSegment ?: ""
            when {
                path.endsWith(".png", ignoreCase = true) -> "image/png"
                path.endsWith(".webp", ignoreCase = true) -> "image/webp"
                path.endsWith(".gif", ignoreCase = true) -> "image/gif"
                else -> "image/jpeg"
            }
        }
        if (!sourceMime.startsWith("image/")) return null
        return try {
            compressImageForUpload(uri, sourceMime)
        } catch (e: OutOfMemoryError) {
            android.util.Log.e("AIAPP", "Image compress OOM: $uri", e)
            null
        } catch (e: Exception) {
            android.util.Log.e("AIAPP", "readFile err: ${e.message}", e)
            null
        }
    }

    private fun compressImageForUpload(uri: android.net.Uri, sourceMime: String): ImagePart? {
        val cr = getApplication<android.app.Application>().contentResolver
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val opts = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_UPLOAD_IMAGE_EDGE, MAX_UPLOAD_IMAGE_EDGE)
        }
        val bitmap = cr.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, opts) } ?: return null
        return try {
            val usePng = sourceMime.contains("png") && bitmap.hasAlpha()
            val format = if (usePng) android.graphics.Bitmap.CompressFormat.PNG else android.graphics.Bitmap.CompressFormat.JPEG
            val mime = if (usePng) "image/png" else "image/jpeg"
            val qualities = if (usePng) intArrayOf(PNG_UPLOAD_QUALITY) else intArrayOf(82, 74, 66, 58)
            var best: ByteArray? = null
            for (quality in qualities) {
                val bytes = java.io.ByteArrayOutputStream().use { stream ->
                    bitmap.compress(format, quality, stream)
                    stream.toByteArray()
                }
                best = bytes
                if (bytes.size <= TARGET_UPLOAD_IMAGE_BYTES || usePng) break
            }
            val bytes = best ?: return null
            if (bytes.size > MAX_UPLOAD_IMAGE_BYTES) return null
            ImagePart(bytes, mime)
        } finally {
            bitmap.recycle()
        }
    }

    private fun persistUploadImage(part: ImagePart): String? {
        return try {
            val app = getApplication<android.app.Application>()
            val ext = if (part.mimeType.contains("png")) "png" else "jpg"
            val dir = java.io.File(app.filesDir, "images/uploads")
            dir.mkdirs()
            val file = java.io.File(dir, "upload_${System.currentTimeMillis()}_${part.bytes.size}.$ext")
            java.io.FileOutputStream(file).use { it.write(part.bytes) }
            androidx.core.content.FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file).toString()
        } catch (e: Exception) {
            android.util.Log.w("AIAPP", "persist upload image failed: ${e.message}")
            null
        }
    }

    private fun messagesJsonSizeBytes(): Long {
        val file = java.io.File(getApplication<android.app.Application>().filesDir, "messages.json")
        return if (file.exists()) file.length() else 0L
    }

    private fun availableMemoryMb(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())) / (1024L * 1024L)
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
        currentUserMessageId: String,
        deepThink: Boolean = false
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
            modelName = model?.let { m -> m.displayName.ifBlank { m.modelName } },
            metadata = if (deepThink) "deepThink=true" else null)
        InMemoryStore.insertMessage(rm)

        streamJob = viewModelScope.launch {
            val deltaBuffer = StringBuilder()
            val streamedContent = StringBuilder()
            val reasoningDeltaBuffer = StringBuilder()
            val streamedReasoning = StringBuilder()
            var lastFlushAt = 0L
            var lastReasoningFlushAt = 0L
            val lastChunkAt = AtomicLong(System.currentTimeMillis())
            val idleWarningShown = AtomicBoolean(false)
            fun updateStreamIdleWarning(enabled: Boolean) {
                if (idleWarningShown.getAndSet(enabled) == enabled) return
                _messages.update { list ->
                    list.map {
                        if (it.id == rm.id) {
                            val metadata = if (enabled) {
                                appendMetadataFlag(it.metadata, "streamIdle=true")
                            } else {
                                removeMetadataFlag(it.metadata, "streamIdle=true")
                            }
                            it.copy(metadata = metadata)
                        } else {
                            it
                        }
                    }
                }
            }
            val idleWatchdog = launch {
                while (true) {
                    delay(STREAM_IDLE_CHECK_MS)
                    if (System.currentTimeMillis() - lastChunkAt.get() >= STREAM_IDLE_WARNING_MS) {
                        updateStreamIdleWarning(true)
                    }
                }
            }
            fun flushBufferedContent() {
                if (deltaBuffer.isEmpty()) return
                val text = deltaBuffer.toString()
                deltaBuffer.clear()
                streamedContent.append(text)
                val content = streamedContent.toString()
                _messages.update { list ->
                    list.map { if (it.id == rm.id) it.copy(content = content) else it }
                }
                lastFlushAt = System.currentTimeMillis()
            }
            fun flushBufferedReasoning() {
                if (reasoningDeltaBuffer.isEmpty()) return
                val text = reasoningDeltaBuffer.toString()
                reasoningDeltaBuffer.clear()
                streamedReasoning.append(text)
                val reasoning = streamedReasoning.toString()
                _messages.update { list ->
                    list.map { if (it.id == rm.id) it.copy(reasoningContent = reasoning) else it }
                }
                lastReasoningFlushAt = System.currentTimeMillis()
            }
            fun appendBufferedContent(delta: String) {
                deltaBuffer.append(delta)
                val now = System.currentTimeMillis()
                if (now - lastFlushAt >= CONTENT_STREAM_FLUSH_INTERVAL_MS || deltaBuffer.length >= CONTENT_STREAM_FLUSH_CHARS) {
                    flushBufferedContent()
                }
            }
            fun appendBufferedReasoning(delta: String) {
                reasoningDeltaBuffer.append(delta)
                val now = System.currentTimeMillis()
                if (now - lastReasoningFlushAt >= REASONING_STREAM_FLUSH_INTERVAL_MS || reasoningDeltaBuffer.length >= REASONING_STREAM_FLUSH_CHARS) {
                    flushBufferedReasoning()
                }
            }

            try {
                val conversationMessages = buildConversationMessages(currentUserMessageId)
                val currentUserText = userText.take(MAX_USER_PROMPT_CHARS)
                val systemPrompt = if (deepThink) {
                    RICH_MARKDOWN_SYSTEM_PROMPT.trim() + "\n\n" + DEEP_THINK_SYSTEM_PROMPT.trim()
                } else {
                    RICH_MARKDOWN_SYSTEM_PROMPT.trim()
                }
                val history = listOf(ChatMessageItem("system", systemPrompt)) +
                    conversationMessages +
                    ChatMessageItem("user", currentUserText, images = images)
                val cfg = config.copy()
                val extraParams = deepThinkExtraParams(deepThink)

                withContext(Dispatchers.IO) {
                    withTimeout(300_000) {
                        chatService.streamChat(history, cfg, model.modelName, extraParams).collect { chunk ->
                            if (chunk.reasoningContent != null || chunk.content != null || chunk.error != null || chunk.isDone) {
                                lastChunkAt.set(System.currentTimeMillis())
                                updateStreamIdleWarning(false)
                            }
                            if (chunk.reasoningContent != null) {
                                appendBufferedReasoning(chunk.reasoningContent)
                            }
                            if (chunk.content != null) {
                                appendBufferedContent(chunk.content)
                            }
                            when {
                                chunk.error != null -> {
                                    flushBufferedContent()
                                    flushBufferedReasoning()
                                    updateStreamIdleWarning(false)
                                    finishWithError(rm, chunk.error)
                                }
                                chunk.isDone -> {
                                    flushBufferedContent()
                                    flushBufferedReasoning()
                                    updateStreamIdleWarning(false)
                                    if (chunk.usage != null) {
                                        _messages.update { list -> list.map { if (it.id == rm.id) it.copy(promptTokens = chunk.usage!!.promptTokens, completionTokens = chunk.usage!!.completionTokens, tokenCount = chunk.usage!!.totalTokens) else it } }
                                    }
                                    finishStreaming(rm)
                                }
                            }
                        }
                    } // withTimeout
                }
            } catch (e: TimeoutCancellationException) {
                flushBufferedContent()
                flushBufferedReasoning()
                updateStreamIdleWarning(false)
                finishWithError(rm, "请求超时，中转站或上游模型长时间没有完成返回。")
            } catch (e: CancellationException) {
                flushBufferedContent()
                flushBufferedReasoning()
                updateStreamIdleWarning(false)
                finishStreaming(rm)
            } catch (e: UnknownHostException) {
                flushBufferedContent()
                flushBufferedReasoning()
                updateStreamIdleWarning(false)
                finishWithError(rm, "无法连接: ${e.message}")
            } catch (e: ConnectException) {
                flushBufferedContent()
                flushBufferedReasoning()
                updateStreamIdleWarning(false)
                finishWithError(rm, "连接被拒绝")
            } catch (e: SocketTimeoutException) {
                flushBufferedContent()
                flushBufferedReasoning()
                updateStreamIdleWarning(false)
                finishWithError(rm, "请求超时")
            } catch (e: java.io.IOException) {
                flushBufferedContent()
                flushBufferedReasoning()
                updateStreamIdleWarning(false)
                finishWithError(rm, "网络异常: ${e.message}")
            } catch (e: Exception) {
                flushBufferedContent()
                flushBufferedReasoning()
                updateStreamIdleWarning(false)
                finishWithError(rm, e.message ?: "未知错误")
            } finally {
                idleWatchdog.cancel()
            }
        }
    }

    private fun deepThinkExtraParams(enabled: Boolean): Map<String, String> {
        if (!enabled) return emptyMap()
        return mapOf(
            "deep_think" to "true",
            "reasoning_effort" to DEEP_THINK_REASONING_EFFORT,
            "thinking_budget_tokens" to DEEP_THINK_BUDGET_TOKENS
        )
    }

    private suspend fun streamFinalAnswerFromReasoning(
        target: Message,
        sourceUser: Message,
        reasoning: String,
        config: com.aiaggregator.app.data.model.ApiConfig,
        model: ModelConfig
    ) {
        val deltaBuffer = StringBuilder()
        val streamedContent = StringBuilder()
        var lastFlushAt = 0L
        fun flushBufferedContent() {
            if (deltaBuffer.isEmpty()) return
            val text = deltaBuffer.toString()
            deltaBuffer.clear()
            streamedContent.append(text)
            val content = streamedContent.toString()
            _messages.update { list ->
                list.map { if (it.id == target.id) it.copy(content = content) else it }
            }
            lastFlushAt = System.currentTimeMillis()
        }
        fun appendBufferedContent(delta: String) {
            deltaBuffer.append(delta)
            val now = System.currentTimeMillis()
            if (now - lastFlushAt >= CONTENT_STREAM_FLUSH_INTERVAL_MS || deltaBuffer.length >= CONTENT_STREAM_FLUSH_CHARS) {
                flushBufferedContent()
            }
        }

        try {
            val repairPrompt = buildString {
                append("用户原始问题：\n")
                append(sourceUser.content.ifBlank { "上一轮用户问题包含图片或附件；请结合已收到的思考摘要补写最终答案。" }.take(MAX_USER_PROMPT_CHARS))
                append("\n\n已收到的思考摘要：\n")
                append(reasoning.take(MAX_CONTEXT_CHARS))
                append("\n\n请只输出最终答案，不要输出思考过程。")
            }
            val history = listOf(
                ChatMessageItem("system", FINAL_FROM_REASONING_SYSTEM_PROMPT.trim()),
                ChatMessageItem("user", repairPrompt)
            )
            withContext(Dispatchers.IO) {
                withTimeout(180_000) {
                    chatService.streamChat(history, config.copy(), model.modelName, emptyMap()).collect { chunk ->
                        if (chunk.content != null) {
                            appendBufferedContent(chunk.content)
                        }
                        when {
                            chunk.error != null -> {
                                flushBufferedContent()
                                finishWithError(target, chunk.error)
                            }
                            chunk.isDone -> {
                                flushBufferedContent()
                                if (chunk.usage != null) {
                                    _messages.update { list ->
                                        list.map {
                                            if (it.id == target.id) {
                                                it.copy(
                                                    promptTokens = chunk.usage.promptTokens,
                                                    completionTokens = chunk.usage.completionTokens,
                                                    tokenCount = chunk.usage.totalTokens
                                                )
                                            } else {
                                                it
                                            }
                                        }
                                    }
                                }
                                finishStreaming(target)
                            }
                        }
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            flushBufferedContent()
            finishWithError(target, "请求超时，中转站或上游模型长时间没有完成返回。")
        } catch (e: CancellationException) {
            flushBufferedContent()
            finishStreaming(target)
        } catch (e: UnknownHostException) {
            flushBufferedContent()
            finishWithError(target, "无法连接: ${e.message}")
        } catch (e: ConnectException) {
            flushBufferedContent()
            finishWithError(target, "连接被拒绝")
        } catch (e: SocketTimeoutException) {
            flushBufferedContent()
            finishWithError(target, "请求超时")
        } catch (e: java.io.IOException) {
            flushBufferedContent()
            finishWithError(target, "网络异常: ${e.message}")
        } catch (e: Exception) {
            flushBufferedContent()
            finishWithError(target, e.message ?: "未知错误")
        }
    }

    private fun buildConversationMessages(currentUserMessageId: String): List<ChatMessageItem> {
        val recent = _messages.value
            .asReversed()
            .asSequence()
            .filter { it.role != MessageRole.SYSTEM && it.id != currentUserMessageId && it.status != MessageStatus.STREAMING }
            .take(MAX_CONTEXT_MESSAGES)
            .toList()
            .asReversed()
        val result = ArrayList<ChatMessageItem>(recent.size)
        var usedChars = 0
        for (message in recent) {
            val content = message.content.take(MAX_MESSAGE_CONTEXT_CHARS)
            if (content.isBlank()) continue
            val remaining = MAX_CONTEXT_CHARS - usedChars
            if (remaining <= 0) break
            val trimmed = content.take(remaining)
            result.add(ChatMessageItem(message.role.name.lowercase(), trimmed))
            usedChars += trimmed.length
        }
        return result
    }

    private fun appendContent(rm: Message, delta: String) {
        _messages.update { list ->
            list.map { if (it.id == rm.id) it.copy(content = it.content + delta) else it }
        }
    }

    private fun finishStreaming(rm: Message) {
        val current = _messages.value.find { it.id == rm.id } ?: rm
        val extracted = extractThinkBlocks(current.content)
        val mergedReasoning = listOfNotNull(
            current.reasoningContent?.takeIf { it.isNotBlank() },
            extracted.reasoningContent?.takeIf { it.isNotBlank() }
        ).joinToString("\n\n").takeIf { it.isNotBlank() }
        val shouldCleanReasoningLabels = hasMetadataFlag(current.metadata, "deepThink=true") ||
            hasMetadataFlag(current.metadata, "reasoningOnlyCompletion=true")
        val cleanedContent = if (shouldCleanReasoningLabels) {
            stripVisibleReasoningHeadings(extracted.content)
        } else {
            extracted.content
        }.trim()
        val final = if (cleanedContent.isBlank() && mergedReasoning.isNullOrBlank()) {
            current.copy(
                content = "⚠️ 未收到模型返回内容。可能是模型只返回了工具调用/思考内容、联网搜索结果未透传，或中转站过滤了正文。请点击重新生成或切换模型/平台后重试。",
                status = MessageStatus.DONE,
                contentType = ContentType.ERROR
            )
        } else if (cleanedContent.isBlank()) {
            current.copy(
                content = REASONING_ONLY_FALLBACK_TEXT,
                reasoningContent = mergedReasoning,
                status = MessageStatus.DONE,
                metadata = appendMetadataFlag(current.metadata, "reasoningOnly=true")
            )
        } else {
            current.copy(
                content = cleanedContent,
                reasoningContent = mergedReasoning,
                status = MessageStatus.DONE,
                metadata = removeMetadataFlag(
                    removeMetadataFlag(current.metadata, "reasoningOnly=true"),
                    "reasoningOnlyCompletion=true"
                )
            )
        }
        _messages.update { list ->
            list.map { if (it.id == rm.id) final else it }
        }
        InMemoryStore.insertMessage(final, saveImmediately = true)
    }

    private data class ThinkExtraction(
        val content: String,
        val reasoningContent: String?
    )

    private fun extractThinkBlocks(text: String): ThinkExtraction {
        if (!text.contains("<think>", ignoreCase = true)) return ThinkExtraction(text, null)
        val regex = Regex("<think>(.*?)</think>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val reasoning = regex.findAll(text)
            .map { it.groupValues.getOrNull(1).orEmpty().trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
        var cleaned = regex.replace(text, "").trim()
        val openIndex = cleaned.indexOf("<think>", ignoreCase = true)
        if (openIndex >= 0) {
            val extraReasoning = cleaned.substring(openIndex + "<think>".length).trim()
            cleaned = cleaned.substring(0, openIndex).trim()
            val mergedReasoning = listOf(reasoning, extraReasoning)
                .filter { it.isNotBlank() }
                .joinToString("\n\n")
            return ThinkExtraction(cleaned, mergedReasoning.takeIf { it.isNotBlank() })
        }
        return ThinkExtraction(cleaned, reasoning.takeIf { it.isNotBlank() })
    }

    private fun stripVisibleReasoningHeadings(text: String): String {
        return text
            .lineSequence()
            .filterNot { isVisibleReasoningHeading(it) }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun isVisibleReasoningHeading(line: String): Boolean {
        val normalized = line
            .trim()
            .trim('#', '*', '-', ' ', ':', '：')
            .lowercase()
        return normalized in setOf(
            "思考过程",
            "思考摘要",
            "推理摘要",
            "可读推理摘要",
            "reasoning summary",
            "readable reasoning summary",
            "thinking process",
            "chain of thought"
        )
    }

    private fun hasMetadataFlag(metadata: String?, flag: String): Boolean {
        return metadata
            ?.split(';')
            ?.map { it.trim() }
            ?.contains(flag) == true
    }

    private fun appendMetadataFlag(metadata: String?, flag: String): String {
        val parts = metadata
            ?.split(';')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        return (parts + flag).distinct().joinToString(";")
    }

    private fun removeMetadataFlag(metadata: String?, flag: String): String? {
        return metadata
            ?.split(';')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() && it != flag }
            ?.joinToString(";")
            ?.takeIf { it.isNotBlank() }
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
                        _genHint.value = "正在准备上一张参考图..."
                        val prevBytes = withContext(Dispatchers.IO) { downloadImage(lastUrl) }
                        if (prevBytes != null) {
                            val prevMime = guessMimeFromUrl(lastUrl)
                            refImages.add(ImagePart(prevBytes, prevMime))
                        }
                    }
                }

                val result = if (refImages.isNotEmpty()) {
                    _genHint.value = "正在根据 ${refImages.size} 张参考图等待生成结果..."
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
