package com.aiaggregator.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.aiaggregator.app.R
import com.aiaggregator.app.base.utils.TimeUtil
import com.aiaggregator.app.data.model.Message
import com.aiaggregator.app.data.model.MessageRole
import com.aiaggregator.app.data.model.MessageStatus
import com.aiaggregator.app.data.model.ModelCategory
import com.aiaggregator.app.data.model.ModelConfig
import com.aiaggregator.app.ui.chat.ChatViewModel
import com.aiaggregator.app.ui.config.ConfigViewModel
import com.aiaggregator.app.ui.settings.AboutScreen
import com.aiaggregator.app.ui.settings.DataScreen
import com.aiaggregator.app.ui.settings.SettingsScreen
import com.aiaggregator.app.ui.theme.AiAggregatorTheme
import kotlinx.coroutines.launch

private enum class Screen { CHAT, SETTINGS, CONFIG, DATA, ABOUT }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.aiaggregator.app.data.local.InMemoryStore.initialize(this)
        setContent { AiAggregatorTheme { MainApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val chatVM: ChatViewModel = viewModel()
    val configVM: ConfigViewModel = viewModel()
    val sessions by chatVM.sessions.collectAsState()
    val messages by chatVM.messages.collectAsState()
    val activeModel by chatVM.activeModel.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var attachedFile by remember { mutableStateOf<Uri?>(null) }
    var editImage by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.CHAT) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val kbController = LocalSoftwareKeyboardController.current
    val generating = messages.lastOrNull()?.status == MessageStatus.STREAMING

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) attachedFile = uri
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(ctx.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            // Use FileProvider to get a content:// URI that Coil and ContentResolver can both read
            attachedFile = androidx.core.content.FileProvider.getUriForFile(
                ctx, "${ctx.packageName}.fileprovider", file
            )
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(ctx, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || sessions.isNotEmpty(),
        drawerContent = {
            DrawerSheet(
                sessions = sessions,
                currentId = chatVM.currentSessionId,
                onNew = {
                    chatVM.createNewSession()
                    scope.launch { currentScreen = Screen.CHAT; drawerState.close() }
                },
                onSelect = { sid ->
                    chatVM.switchSession(sid)
                    scope.launch { currentScreen = Screen.CHAT; drawerState.close() }
                },
                onDelete = { sid -> chatVM.deleteSession(sid) },
                onSettings = {
                    scope.launch { currentScreen = Screen.SETTINGS; drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen == Screen.CHAT) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, "菜单")
                            }
                        },
                        title = {
                            Text(
                                chatVM.currentSessionTitle ?: "清畅AI",
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            IconButton(onClick = { chatVM.createNewSession() }) {
                                Icon(Icons.Filled.Add, "新建")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { currentScreen = Screen.CHAT }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                            }
                        },
                        title = {
                            Text(
                                when (currentScreen) {
                                    Screen.SETTINGS -> "设置"
                                    Screen.CONFIG -> "API 配置"
                                    Screen.DATA -> "数据管理"
                                    Screen.ABOUT -> "关于"
                                    else -> ""
                                }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).clickable { kbController?.hide() }.focusable()) {
                when (currentScreen) {
                    Screen.CHAT -> Column(Modifier.fillMaxSize().imePadding()) {
                        Box(Modifier.weight(1f)) {
                            ChatView(messages, ctx, activeModel, onSuggestionClick = { inputText = it }, onRegenerate = { chatVM.regenerate() })
                        }
                        // Pre-send warning: image attached to non-vision model
                        if (attachedFile != null && activeModel?.category != ModelCategory.IMAGE) {
                            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)) {
                                Text(
                                    "⚠️ 当前是文字模型，不支持图片识别。请切换到视觉模型或图片生成模型。",
                                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        // Edit-image toggle for IMAGE models
                        val hasPrevImage = activeModel?.category == ModelCategory.IMAGE &&
                            messages.lastOrNull { it.role == MessageRole.ASSISTANT && !it.imageUrl.isNullOrBlank() } != null
                        if (hasPrevImage) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {
                                ToggleChip(
                                    selected = editImage,
                                    onToggle = { editImage = !editImage },
                                    icon = { Icon(Icons.Filled.Edit, null, Modifier.size(16.dp)) },
                                    label = if (editImage) "编辑中" else "编辑上一张图"
                                )
                            }
                        }
                        InputArea(
                            model = activeModel, available = chatVM.availableModels,
                            onSelect = { chatVM.switchModel(it) },
                            text = inputText, onTextChange = { inputText = it },
                            onSend = {
                                kbController?.hide()
                                if (inputText.isNotBlank()) {
                                    chatVM.sendMessage(inputText, attachedFile, editImage = editImage)
                                    inputText = ""; attachedFile = null; editImage = false
                                }
                            },
                            generating = generating, attached = attachedFile,
                            onClearFile = { attachedFile = null },
                            onPickFile = { filePicker.launch("image/*") },
                            onTakePhoto = {
                                if (androidx.core.content.ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
                                    cameraLauncher.launch(null)
                                else cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            onStop = { chatVM.stopGeneration() }
                        )
                    }
                    Screen.SETTINGS -> SettingsScreen(
                        onNavConfig = { currentScreen = Screen.CONFIG },
                        onNavData = { currentScreen = Screen.DATA },
                        onNavAbout = { currentScreen = Screen.ABOUT }
                    )
                    Screen.CONFIG -> ConfigScreen(
                        vm = configVM,
                        onBack = { currentScreen = Screen.SETTINGS }
                    )
                    Screen.DATA -> DataScreen()
                    Screen.ABOUT -> AboutScreen()
                }
            }
        }
    }
}

// ── Chat ──

@Composable
private fun ChatView(messages: List<Message>, ctx: Context, activeModel: ModelConfig?, onSuggestionClick: (String) -> Unit, onRegenerate: () -> Unit = {}) {
    if (messages.isEmpty()) {
        val suggestionPool = remember {
            listOf(
                "用简单的语言解释量子计算", "写一首关于夏天的诗", "帮我规划三天北京旅行",
                "推荐几本好看的科幻小说", "怎么做番茄炒蛋", "Python 和 Java 哪个更适合初学者",
                "用一句话解释黑洞", "帮我写一封辞职邮件", "最近有什么好看的电影",
                "如何提高英语口语", "解释什么是机器学习", "讲一个睡前故事",
                "推荐适合新手的健身计划", "30分钟能做哪些快手菜"
            )
        }
        val shown = remember { suggestionPool.shuffled().take(4) }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp))) { Image(painter = painterResource(R.drawable.bjlg), contentDescription = null, modifier = Modifier.fillMaxSize()); Image(painter = painterResource(R.drawable.qjlg), contentDescription = "清畅AI", modifier = Modifier.fillMaxSize()) }
            Spacer(Modifier.height(24.dp))
            Text("清畅AI", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "多平台 AI 模型，一站式对话",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (activeModel != null) {
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        activeModel.displayName.ifBlank { activeModel.modelName },
                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Text("试试这些", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            shown.forEach { s ->
                Surface(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSuggestionClick(s) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(s, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        LaunchedEffect(messages.size) { listState.animateScrollToItem(0) }
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages.reversed(), key = { it.id }) { msg ->
                val idx = messages.indexOf(msg)
                val prev = if (idx > 0) messages[idx - 1].role else null
                val next = if (idx < messages.lastIndex) messages[idx + 1].role else null
                ChatBubble(msg, prev, next, ctx, onRegenerate)
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: Message, prev: MessageRole?, next: MessageRole?, ctx: Context, onRegenerate: () -> Unit = {}) {
    val isUser = msg.role == MessageRole.USER
    val streaming = msg.status == MessageStatus.STREAMING
    val first = prev != msg.role
    val last = next != msg.role
    val tr = if (first) 20.dp else if (isUser) 20.dp else 4.dp
    val br = if (last) 20.dp else if (isUser) 4.dp else 20.dp
    val shape = RoundedCornerShape(
        topStart = tr, topEnd = tr,
        bottomStart = if (isUser) 20.dp else br,
        bottomEnd = if (isUser) br else 20.dp
    )
    Row(
        Modifier.fillMaxWidth().padding(
            horizontal = 16.dp,
            vertical = if (last) 8.dp else 1.dp
        ),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            Modifier.widthIn(max = 320.dp),
            shape = shape,
            tonalElevation = if (!isUser) 1.dp else 0.dp,
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column {
                // Generating shimmer for image
                if (!isUser && streaming && msg.contentType == com.aiaggregator.app.data.model.ContentType.IMAGE) {
                    ImageShimmer()
                }
                if (!msg.imageUrl.isNullOrBlank()) {
                    var showFull by remember { mutableStateOf(false) }
                    // Image card with rounded corners and shadow
                    Surface(
                        Modifier.widthIn(max = 300.dp).padding(6.dp),
                        shape = RoundedCornerShape(14.dp),
                        tonalElevation = 2.dp
                    ) {
                        Box {
                            AsyncImage(
                                model = msg.imageUrl,
                                contentDescription = "生成的图片",
                                modifier = Modifier.widthIn(max = 300.dp).clickable { showFull = true },
                                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                                error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                            )
                            // Download button overlay
                            Surface(
                                Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.5f)
                            ) {
                                IconButton(
                                    onClick = { saveImageToDevice(ctx, msg.imageUrl!!) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Download, "保存", Modifier.size(16.dp), tint = Color.White)
                                }
                            }
                        }
                    }
                    // Fullscreen zoomable viewer
                    if (showFull) {
                        var scale by remember { mutableStateOf(1f) }
                        var offsetX by remember { mutableStateOf(0f) }
                        var offsetY by remember { mutableStateOf(0f) }
                        Dialog(
                            onDismissRequest = { showFull = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
                        ) {
                            Box(Modifier.fillMaxSize().background(Color.Black)) {
                                AsyncImage(
                                    model = msg.imageUrl,
                                    contentDescription = "全屏查看",
                                    modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(1f, 5f)
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        }
                                    }.graphicsLayer {
                                        scaleX = scale; scaleY = scale
                                        translationX = offsetX
                                        translationY = offsetY
                                    },
                                    placeholder = ColorPainter(Color.DarkGray)
                                )
                                // Top bar: close + share + save
                                Row(Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 16.dp)) {
                                    IconButton(onClick = {
                                        shareContent(ctx, msg.imageUrl!!, true)
                                    }) {
                                        Icon(Icons.Filled.Share, "分享", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { saveImageToDevice(ctx, msg.imageUrl!!); Toast.makeText(ctx, "正在保存...", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Filled.Download, "保存", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { showFull = false }) {
                                        Icon(Icons.Filled.Close, "关闭", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
                                    }
                                }
                                // Double-tap to reset zoom
                                Text("双击重置", Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
                if (msg.content.isNotBlank()) {
                    if (isUser) {
                        Text(msg.content, Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        val hColor = MaterialTheme.colorScheme.primary
                        val qColor = MaterialTheme.colorScheme.onSurfaceVariant
                        val cColor = MaterialTheme.colorScheme.secondary
                        val annotated = parseMarkdown(if (streaming) msg.content + "▌" else msg.content, hColor, qColor, cColor)
                        Text(
                            annotated,
                            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (!isUser && streaming && msg.content.isEmpty()) {
                    TypingDots()
                }
                if (!isUser && last) {
                    // Token + model info
                    if (msg.modelName != null || msg.tokenCount != null) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), horizontalArrangement = Arrangement.Start) {
                            Text(
                                buildString {
                                    if (msg.modelName != null) append("🤖 ${msg.modelName}")
                                    if (msg.promptTokens != null && msg.completionTokens != null) {
                                        if (msg.modelName != null) append(" · ")
                                        append("↑${msg.promptTokens} ↓${msg.completionTokens}")
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(end = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Regenerate button — only on last AI message
                        if (!isUser && last) {
                            IconButton(onClick = { onRegenerate() }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Refresh, "重新生成", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { shareContent(ctx, msg.imageUrl ?: msg.content, msg.imageUrl != null) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Share, "分享", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { copyText(ctx, msg.imageUrl ?: msg.content) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.ContentCopy, "复制", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageShimmer() {
    val infinite = rememberInfiniteTransition()
    val shimmerX by infinite.animateFloat(
        initialValue = -200f, targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceVariant
        ),
        start = androidx.compose.ui.geometry.Offset(shimmerX, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmerX + 200f, 0f)
    )
    Box(
        Modifier.widthIn(max = 300.dp)
            .height(200.dp)
            .padding(8.dp)
            .background(brush, RoundedCornerShape(12.dp))
    )
}

@Composable
private fun TypingDots() {
    val infinite = rememberInfiniteTransition()
    Row(
        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val alpha by infinite.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = i * 150),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                Modifier.size(8.dp).background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    MaterialTheme.shapes.extraLarge
                )
            )
        }
    }
}

@Composable
private fun ToggleChip(selected: Boolean, onToggle: () -> Unit, icon: @Composable () -> Unit, label: String) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = Modifier.clickable { onToggle() },
        shape = RoundedCornerShape(20.dp),
        color = bg
    ) {
        Row(
            Modifier.padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated check when selected
            androidx.compose.animation.AnimatedVisibility(selected) {
                Icon(Icons.Filled.Check, null, Modifier.size(14.dp).padding(end = 4.dp), tint = fg)
            }
            icon()
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
        }
    }
}

private fun copyText(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("message", text))
    Toast.makeText(ctx, "已复制", Toast.LENGTH_SHORT).show()
}

private fun shareContent(ctx: Context, text: String, isImage: Boolean) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = if (isImage) "text/plain" else "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(android.content.Intent.createChooser(intent, "分享到"))
}

private fun parseMarkdown(text: String, hColor: Color, qColor: Color, cColor: Color): androidx.compose.ui.text.AnnotatedString {
    val lines = text.split('\n')
    return buildAnnotatedString {
        var inCodeBlock = false
        lines.forEachIndexed { idx, line ->
            if (idx > 0) append('\n')
            val trimmed = line.trimStart()
            when {
                // ── Code block boundary ──
                trimmed.startsWith("```") -> {
                    inCodeBlock = !inCodeBlock
                    if (inCodeBlock) {
                        withStyle(SpanStyle(color = cColor)) { append("┌── 代码 ──") }
                    } else {
                        withStyle(SpanStyle(color = cColor)) { append("└────────") }
                    }
                }
                // ── Inside code block ──
                inCodeBlock -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = cColor)) {
                        append(line)
                    }
                }
                // ── # H1 heading ──
                trimmed.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = hColor)) {
                        append("◆ ${trimmed.removePrefix("# ")}")
                    }
                }
                // ── ## H2 heading ──
                trimmed.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = hColor)) {
                        append("▸ ${trimmed.removePrefix("## ")}")
                    }
                }
                // ── ### H3 heading ──
                trimmed.startsWith("###") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = hColor)) {
                        append("▪ ${trimmed.removePrefix("###").trimStart()}")
                    }
                }
                // ── > Blockquote ──
                trimmed.startsWith("> ") -> {
                    withStyle(SpanStyle(color = qColor)) {
                        append("▎${trimmed.removePrefix("> ")}")
                    }
                }
                // ── Horizontal rule ──
                trimmed == "---" || trimmed == "***" -> {
                    withStyle(SpanStyle(color = qColor.copy(alpha = 0.3f))) {
                        append("─".repeat(28))
                    }
                }
                // ── 1. Ordered list ──
                trimmed.matches(Regex("^\\d+\\..*")) -> {
                    val num = trimmed.substringBefore(".")
                    append("  $num. ")
                    appendInlineStyles(trimmed.substringAfter(". "))
                }
                // ── - [x] Checkbox done ──
                trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
                    withStyle(SpanStyle(color = hColor)) { append("  ✅  ") }
                    appendInlineStyles(trimmed.removePrefix("- [x] ").removePrefix("- [X] "))
                }
                // ── - [ ] Checkbox todo ──
                trimmed.startsWith("- [ ] ") -> {
                    append("  ☐  ")
                    appendInlineStyles(trimmed.removePrefix("- [ ] "))
                }
                // ── - Unordered list ──
                trimmed.startsWith("- ") -> {
                    append("  ●  ")
                    appendInlineStyles(trimmed.removePrefix("- "))
                }
                // ── | Table row ──
                trimmed.startsWith("|") -> {
                    append(trimmed.replace("|", " │ "))
                }
                // ── Blank line ──
                trimmed.isBlank() -> append(' ')
                // ── Regular text ──
                else -> appendInlineStyles(trimmed)
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineStyles(text: String) {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            text.startsWith("*", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end > i) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end > i) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> { append(text[i]); i++ }
        }
    }
}

private fun saveImageToDevice(ctx: Context, imageUrl: String) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val client = com.aiaggregator.app.data.remote.HttpClientFactory.create()
            val request = okhttp3.Request.Builder().url(imageUrl).build()
            val response = client.newCall(request).execute()
            val bytes = response.body?.bytes() ?: return@launch
            val fileName = "AI_${System.currentTimeMillis()}.png"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/清畅AI")
                    put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = ctx.contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
                uri?.let {
                    ctx.contentResolver.openOutputStream(it)?.use { out -> out.write(bytes) }
                    values.clear()
                    values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                    ctx.contentResolver.update(it, values, null, null)
                }
            } else {
                @Suppress("DEPRECATION")
                val dir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES + "/清畅AI"
                )
                dir.mkdirs()
                java.io.File(dir, fileName).writeBytes(bytes)
            }
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(ctx, "图片已保存到相册", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(ctx, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// ── Input ──

@Composable
private fun InputArea(
    model: ModelConfig?,
    available: List<ModelConfig>,
    onSelect: (ModelConfig) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    generating: Boolean,
    attached: Uri?,
    onClearFile: () -> Unit,
    onPickFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onStop: () -> Unit
) {
    Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth()) {
            if (generating) {
                LinearProgressIndicator(
                    Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
            Row(
                Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Model selector - compact
                if (available.isNotEmpty()) {
                    var modelExpanded by remember { mutableStateOf(false) }
                    Surface(
                        Modifier.clickable { modelExpanded = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val label = model?.let { m -> m.displayName.ifBlank { m.modelName } } ?: "模型"
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Filled.ArrowDropDown, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    DropdownMenu(expanded = modelExpanded, onDismissRequest = { modelExpanded = false }) {
                        available.groupBy { it.category }.forEach { (cat, ms) ->
                            DropdownMenuItem(text = { Text(cat.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }, onClick = {}, enabled = false)
                            ms.forEach { m -> DropdownMenuItem(text = { Text(m.displayName.ifBlank { m.modelName }) }, onClick = { onSelect(m); modelExpanded = false }) }
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Row {
                    IconButton(onClick = onPickFile, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.AttachFile, "文件", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = onTakePhoto, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.AddPhotoAlternate, "拍照", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.weight(1f))

                // File attachment preview (shown above model row when attached)
                if (attached != null) {
                    val ctx = LocalContext.current
                    val mimeType = remember(attached) { ctx.contentResolver.getType(attached) ?: "" }
                    val isImage = mimeType.startsWith("image/")
                    Surface(
                        Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isImage) {
                                AsyncImage(
                                    model = attached,
                                    contentDescription = "预览",
                                    modifier = Modifier.size(40.dp).padding(end = 8.dp)
                                )
                            }
                            Text("📎 已选择文件", style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = onClearFile, contentPadding = PaddingValues(0.dp)) {
                                Text("× 移除", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            // Input row
            Row(
                Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val kb = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("输入消息...", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier.weight(1f),
                    maxLines = 5,
                    singleLine = false,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Default
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(6.dp))
                if (generating) {
                    FilledIconButton(
                        onClick = onStop,
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    ) { Icon(Icons.Filled.Stop, "停止") }
                } else {
                    FilledIconButton(
                        onClick = onSend,
                        enabled = text.isNotBlank(),
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    ) { Icon(Icons.AutoMirrored.Filled.Send, "发送") }
                }
            }
        }
    }
}

// ── Drawer ──

@Composable
private fun DrawerSheet(
    sessions: List<com.aiaggregator.app.data.model.Session>,
    currentId: String,
    onNew: () -> Unit,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSettings: () -> Unit
) {
    ModalDrawerSheet(Modifier.width(300.dp)) {
        // Brand header
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))) { Image(painter = painterResource(R.drawable.bjlg), contentDescription = null, modifier = Modifier.fillMaxSize()); Image(painter = painterResource(R.drawable.qjlg), contentDescription = "清畅AI", modifier = Modifier.fillMaxSize()) }
            Spacer(Modifier.height(12.dp))
            Text("清畅AI", style = MaterialTheme.typography.titleLarge)
            Text("开源 · 免费 · 多平台智能对话", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(Modifier.padding(horizontal = 20.dp))

        // New chat button
        Spacer(Modifier.height(4.dp))
        Surface(
            Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { onNew() },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("新建对话", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(4.dp))

        // Session list
        Text(
            "历史对话", Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💬", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("暂无历史对话", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(sessions, key = { it.id }) { s ->
                    val selected = s.id == currentId
                    Surface(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .clickable { onSelect(s.id) },
                        shape = MaterialTheme.shapes.medium,
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (selected) 2.dp else 0.dp
                    ) {
                        Row(
                            Modifier.padding(start = if (selected) 12.dp else 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // leading accent bar for selected
                            if (selected) {
                                Box(
                                    Modifier.width(4.dp).height(32.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(s.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = if (selected) MaterialTheme.typography.labelLarge
                                    else MaterialTheme.typography.bodyMedium)
                                Text(
                                    TimeUtil.formatChatTime(s.lastActiveAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onDelete(s.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Delete, "删除",
                                    Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(4.dp))

        // Settings
        Surface(
            Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { onSettings() },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Settings, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text("设置", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ── Config ──

private fun duplicateModel(vm: ConfigViewModel, m: ModelConfig, p: com.aiaggregator.app.data.model.ApiConfig) {
    val allModels = vm.loadModels()
    val baseName = m.displayName.ifBlank { m.modelName }
    // Find highest 副本N number for this model name
    val pattern = Regex("""^${Regex.escape(baseName)} 副本(\d+)$""")
    val maxN = allModels.mapNotNull { other ->
        pattern.find(other.displayName)?.groupValues?.get(1)?.toIntOrNull()
    }.maxOrNull() ?: 0
    val copyName = "$baseName 副本${maxN + 1}"
    // Create new platform copy
    val newPlatform = p.copy(id = java.util.UUID.randomUUID().toString())
    vm.savePlatform(newPlatform)
    // Create new model copy
    val newModel = m.copy(id = java.util.UUID.randomUUID().toString(), platformId = newPlatform.id, displayName = copyName, isDefault = false)
    vm.saveModel(newModel)
}

private data class Vendor(val id: String, val label: String, val baseUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(vm: ConfigViewModel, onBack: () -> Unit) {
    val models = remember { mutableStateOf(vm.loadModels()) }
    var adding by remember { mutableStateOf(false) }
    var editingModelId by remember { mutableStateOf<String?>(null) }
    var vExp by remember { mutableStateOf(false) }
    val vendors = remember {
        listOf(
            Vendor("openai", "OpenAI 兼容格式", "https://api.openai.com/v1"),
            Vendor("anthropic", "Anthropic 兼容格式", "https://api.anthropic.com"),
            Vendor("deepseek", "DeepSeek (OpenAI兼容)", "https://api.deepseek.com/v1"),
            Vendor("custom", "自定义 (OpenAI兼容)", "")
        )
    }
    var sel by remember { mutableStateOf(vendors.first()) }
    var url by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var mn by remember { mutableStateOf("") }
    var dn by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf(ModelCategory.CHAT) }

    fun refresh() { models.value = vm.loadModels() }
    fun resetForm() {
        sel = vendors.first(); url = ""; key = ""; mn = ""; dn = ""
        cat = ModelCategory.CHAT; adding = false; editingModelId = null
    }
    fun fillForEdit(m: ModelConfig, p: com.aiaggregator.app.data.model.ApiConfig?) {
        editingModelId = m.id
        sel = vendors.find { it.id == "anthropic" && p?.formatType == com.aiaggregator.app.data.model.ApiFormatType.ANTHROPIC_COMPATIBLE }
            ?: vendors.first()
        url = p?.baseUrl ?: ""
        key = p?.apiKey ?: ""
        mn = m.modelName
        dn = m.displayName
        cat = m.category
        adding = true
    }

    Column(
        Modifier.fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("API 配置", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text("管理已配置的模型", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))

        if (models.value.isEmpty() && !adding) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤖", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("暂无模型", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("点击下方按钮添加第一个模型", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            models.value.forEach { m ->
                val p = vm.getPlatform(m.platformId)
                ElevatedCard(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (editingModelId == m.id) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(m.displayName.ifBlank { m.modelName }, style = MaterialTheme.typography.titleSmall)
                                    if (m.isDefault) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary, tonalElevation = 0.dp) {
                                            Text("默认", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Text(m.category.label, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Text(m.modelName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (p != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(p.baseUrl, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            val scope = rememberCoroutineScope()
                            val ctxToast = LocalContext.current
                            var testing by remember { mutableStateOf(false) }
                            FilledTonalButton(onClick = {
                                if (p == null || testing) return@FilledTonalButton
                                testing = true
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        com.aiaggregator.app.business.chat.ChatService().syncChat(
                                            listOf(com.aiaggregator.app.business.adapter.ChatMessageItem("user", "hi")), p, m.modelName
                                        )
                                    }
                                    withContext(Dispatchers.Main) {
                                        testing = false
                                        Toast.makeText(ctxToast, if (result.error == null) "✅ 连接成功" else "❌ ${result.error}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp), enabled = !testing) {
                                Text(if (testing) "测试中..." else "测试", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.width(6.dp))
                            FilledTonalButton(onClick = { vm.setDefaultModel(m.id); refresh() }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
                                Text(if (m.isDefault) "✓ 默认" else "设为默认", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.width(6.dp))
                            FilledTonalButton(onClick = { fillForEdit(m, p) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Filled.Edit, "编辑", Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("编辑", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.width(6.dp))
                            FilledTonalButton(onClick = {
                                if (p != null) {
                                    duplicateModel(vm, m, p); refresh()
                                }
                            }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Filled.ContentCopy, "复制", Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("复制", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.width(6.dp))
                            FilledTonalButton(onClick = { vm.deleteModel(m.id); refresh() }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Filled.Delete, "删除", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(4.dp))
                                Text("删除", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (adding) {
            ElevatedCard(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors()
            ) {
                Column(Modifier.padding(20.dp)) {
            Text(if (editingModelId != null) "编辑模型" else "添加模型", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("填写模型信息以开始使用", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            // Compatibility hint
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Text(
                    "💡 本平台兼容 OpenAI 格式与 Anthropic 格式，支持这两种格式的模型厂商（如 DeepSeek、豆包、通义千问、Moonshot、零一万物、智谱等）或第三方中转站，均可接入使用。",
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(vExp, { vExp = it }) {
                OutlinedTextField(
                    sel.label, {},
                    readOnly = true,
                    label = { Text("选择厂商") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(vExp) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                )
                DropdownMenu(vExp, { vExp = false }) {
                    vendors.forEach { v ->
                        DropdownMenuItem(text = { Text(v.label) }, onClick = {
                            sel = v; if (v.baseUrl.isNotBlank()) url = v.baseUrl; vExp = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(key, { key = it }, label = { Text("API 密钥") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("sk-...") })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(url, { url = it }, label = { Text("API 地址") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(mn, { mn = it }, label = { Text("模型名 (API用)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("gpt-4o / gpt-image-1") })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(dn, { dn = it }, label = { Text("显示名称 (可选)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("如: GPT-4o 工作用") })
            Spacer(Modifier.height(12.dp))
            var catExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(catExpanded, { catExpanded = it }) {
                OutlinedTextField(
                    cat.label, {}, readOnly = true,
                    label = { Text("模型类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                )
                DropdownMenu(catExpanded, { catExpanded = false }) {
                    listOf(ModelCategory.CHAT, ModelCategory.IMAGE).forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.label) },
                            onClick = { cat = c; catExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row {
                TextButton(onClick = { resetForm() }) { Text("取消") }
                Spacer(Modifier.width(12.dp))
                TextButton(
                    onClick = {
                        if (key.isNotBlank() && mn.isNotBlank() && url.isNotBlank()) {
                            val fmt = if (sel.id == "anthropic") com.aiaggregator.app.data.model.ApiFormatType.ANTHROPIC_COMPATIBLE
                            else com.aiaggregator.app.data.model.ApiFormatType.OPENAI_COMPATIBLE
                            val pf = if (editingModelId != null) {
                                val existingModel = vm.loadModels().find { it.id == editingModelId }
                                val existingPlatform = if (existingModel != null) vm.getPlatform(existingModel.platformId) else null
                                com.aiaggregator.app.data.model.ApiConfig(
                                    id = existingPlatform?.id ?: java.util.UUID.randomUUID().toString(),
                                    platformName = sel.label, baseUrl = url, apiKey = key, formatType = fmt
                                )
                            } else {
                                com.aiaggregator.app.data.model.ApiConfig(
                                    platformName = sel.label, baseUrl = url, apiKey = key, formatType = fmt
                                )
                            }
                            vm.savePlatform(pf)
                            vm.saveModel(
                                ModelConfig(
                                    id = editingModelId ?: java.util.UUID.randomUUID().toString(),
                                    platformId = pf.id, displayName = dn, modelName = mn, category = cat
                                )
                            )
                            resetForm(); refresh()
                        }
                    },
                    enabled = key.isNotBlank() && mn.isNotBlank() && url.isNotBlank()
                ) { Text("保存") }
            }
                } // close Column
            } // close ElevatedCard
        } else {
            ElevatedCard(
                Modifier.fillMaxWidth().clickable { resetForm(); adding = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("添加模型", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
