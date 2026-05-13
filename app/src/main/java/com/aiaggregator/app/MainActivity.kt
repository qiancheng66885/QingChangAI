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
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.window.Popup
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.aiaggregator.app.R
import com.aiaggregator.app.base.ext.safeStartActivity
import com.aiaggregator.app.data.local.ImageStorageManager
import com.aiaggregator.app.base.utils.TimeUtil
import com.aiaggregator.app.data.model.Message
import com.aiaggregator.app.data.model.MessageRole
import com.aiaggregator.app.data.model.MessageStatus
import com.aiaggregator.app.data.model.ApiFormatType
import com.aiaggregator.app.data.model.ModelCategory
import com.aiaggregator.app.data.model.ModelConfig
import com.aiaggregator.app.ui.chat.ChatViewModel
import com.aiaggregator.app.ui.config.ConfigEditScreen
import com.aiaggregator.app.ui.config.ConfigViewModel
import com.aiaggregator.app.ui.settings.AboutScreen
import com.aiaggregator.app.ui.settings.DataScreen
import com.aiaggregator.app.ui.settings.SettingsScreen
import com.aiaggregator.app.ui.settings.SupportScreen
import com.aiaggregator.app.ui.theme.AiAggregatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Screen { CHAT, SETTINGS, CONFIG, CONFIG_EDIT, DATA, SUPPORT, ABOUT }
private val testChatService by lazy { com.aiaggregator.app.business.chat.ChatService() }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.aiaggregator.app.data.local.InMemoryStore.initialize(this)
        setContent { AiAggregatorTheme { MainApp() } }
    }

    override fun onPause() {
        super.onPause()
        kotlinx.coroutines.runBlocking { com.aiaggregator.app.data.local.InMemoryStore.flushMessages() }
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
    val genHint by chatVM.genHint.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var attachedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var editImage by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.CHAT) }
    var editingModelId by remember { mutableStateOf<String?>(null) }
    var configRefreshKey by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
    val kbController = LocalSoftwareKeyboardController.current
    val generating = messages.lastOrNull()?.status == MessageStatus.STREAMING

    // Chat should open in read mode. Only an explicit tap on the input field should show keyboard.
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.CHAT) {
            focusManager.clearFocus(force = true)
            kbController?.hide()
        }
    }

    // Keep screen on while generating
    val activity = ctx as? android.app.Activity
    LaunchedEffect(generating) {
        if (generating) activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // 相机全分辨率拍照
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            attachedFiles = attachedFiles + tempPhotoUri!!
        }
        tempPhotoUri = null
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = java.io.File(ctx.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(ctx, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }
    // 系统图片选择器
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uris ->
        if (uris.isNotEmpty()) attachedFiles = attachedFiles + uris
    }
    // 文件选择器
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        attachedFiles = attachedFiles + uris
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
                            IconButton(onClick = {
                                currentScreen = when (currentScreen) {
                                    Screen.SETTINGS -> Screen.CHAT
                                    Screen.CONFIG -> Screen.SETTINGS
                                    Screen.CONFIG_EDIT -> Screen.CONFIG
                                    Screen.DATA -> Screen.SETTINGS
                                    Screen.SUPPORT -> Screen.SETTINGS
                                    Screen.ABOUT -> Screen.SETTINGS
                                    Screen.CHAT -> Screen.CHAT
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                            }
                        },
                        title = {
                            Text(
                                when (currentScreen) {
                                    Screen.SETTINGS -> "设置"
                                    Screen.CONFIG -> "API 配置"
                                    Screen.CONFIG_EDIT -> if (editingModelId == null) "添加模型" else "编辑模型"
                                    Screen.DATA -> "数据管理"
                                    Screen.SUPPORT -> "软件支持与教程"
                                    Screen.ABOUT -> "关于"
                                    else -> ""
                                }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
            },
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus(force = true)
                        kbController?.hide()
                    }
                    .focusable()
            ) {
                when (currentScreen) {
                    Screen.CHAT -> Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            ChatView(
                                messages, ctx, activeModel, genHint = genHint,
                                onSuggestionClick = { inputText = it },
                                onRegenerate = { chatVM.regenerate() },
                                onStop = { chatVM.stopGeneration() },
                                onUserScroll = {
                                    focusManager.clearFocus(force = true)
                                    kbController?.hide()
                                }
                            )
                        }
                        // 仅纯文字模型附图时警告
                        if (attachedFiles.isNotEmpty() && activeModel?.category == ModelCategory.CHAT) {
                            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)) {
                                Text(
                                    "⚠️ 当前是纯文字模型，不支持图片识别。请切换到多模态或图片生成模型。",
                                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        // Edit-image toggle for IMAGE models
                        val hasPrevImage = activeModel?.category == ModelCategory.IMAGE &&
                            messages.lastOrNull { it.role == MessageRole.ASSISTANT && it.allImageUrls.isNotEmpty() } != null
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
                                if (inputText.isNotBlank() || attachedFiles.isNotEmpty()) {
                                    chatVM.sendMessage(inputText.ifBlank { "请描述这些图片" }, attachedFiles, editImage = editImage)
                                    inputText = ""; attachedFiles = emptyList(); editImage = false
                                }
                            },
                            generating = generating, attached = attachedFiles,
                            onClearFile = { idx -> attachedFiles = attachedFiles.toMutableList().also { it.removeAt(idx) } },
                            onClearAllFiles = { attachedFiles = emptyList() },
                            onTakePhoto = {
                                if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    val file = java.io.File(ctx.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickImages = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            onPickFiles = { filePicker.launch("*/*") },
                            onStop = { chatVM.stopGeneration() },
                            onAddImages = { uris -> attachedFiles = attachedFiles + uris }
                        )
                    }
                    Screen.SETTINGS -> SettingsScreen(
                        onNavConfig = { currentScreen = Screen.CONFIG },
                        onNavData = { currentScreen = Screen.DATA },
                        onNavSupport = { currentScreen = Screen.SUPPORT },
                        onNavAbout = { currentScreen = Screen.ABOUT }
                    )
                    Screen.CONFIG -> ConfigScreen(
                        vm = configVM,
                        refreshKey = configRefreshKey,
                        onAddModel = {
                            editingModelId = null
                            currentScreen = Screen.CONFIG_EDIT
                        },
                        onEditModel = { id ->
                            editingModelId = id
                            currentScreen = Screen.CONFIG_EDIT
                        }
                    )
                    Screen.CONFIG_EDIT -> ConfigEditScreen(
                        vm = configVM,
                        modelId = editingModelId,
                        onCancel = { currentScreen = Screen.CONFIG },
                        onSaved = {
                            configRefreshKey++
                            currentScreen = Screen.CONFIG
                        }
                    )
                    Screen.DATA -> DataScreen()
                    Screen.SUPPORT -> SupportScreen()
                    Screen.ABOUT -> AboutScreen()
                }
            }
        }
    }
}

// ── Chat ──

@Composable
private fun ChatView(messages: List<Message>, ctx: Context, activeModel: ModelConfig?, genHint: String = "", onSuggestionClick: (String) -> Unit, onRegenerate: () -> Unit = {}, onStop: () -> Unit = {}, onUserScroll: () -> Unit = {}) {
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
        val lastMsg = messages.lastOrNull()
        val isStreaming = lastMsg?.status == com.aiaggregator.app.data.model.MessageStatus.STREAMING
        val coroutineScope = rememberCoroutineScope()

        // ─── 跟随控制 ───
        var userScrolledUp by remember { mutableStateOf(false) }
        var followLocked by remember { mutableStateOf(false) }
        // 内容是否填满了屏幕（AI 第一行被推出顶部）
        var contentFilled by remember { mutableStateOf(false) }
        // 是否是程序触发的滚动（按钮/自动跟随），用于区分用户手势
        var programmaticScroll by remember { mutableStateOf(false) }

        // 检测内容填满屏幕
        LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            if (listState.firstVisibleItemIndex >= messages.lastIndex
                && listState.firstVisibleItemScrollOffset > 0) {
                contentFilled = true
            }
        }

        // 检测用户滚动
        LaunchedEffect(listState) {
            snapshotFlow { listState.isScrollInProgress to listState.canScrollForward }
                .collect { (scrolling, canForward) ->
                    if (scrolling && canForward && !programmaticScroll) {
                        // 用户上划 → 暂停跟随，并退出输入状态
                        onUserScroll()
                        userScrolledUp = true
                        followLocked = false
                    } else if (!scrolling && !canForward && !programmaticScroll) {
                        // 用户手动滚回底部 → 恢复永久跟随
                        userScrolledUp = false
                        followLocked = true
                        contentFilled = false
                    }
                }
        }

        // ─── 按钮：contentFilled 后防抖 canScrollForward + 锁定 ───
        var canScrollDebounced by remember { mutableStateOf(false) }
        var buttonLocked by remember { mutableStateOf(false) }

        LaunchedEffect(contentFilled, listState.canScrollForward) {
            if (contentFilled && listState.canScrollForward) {
                delay(500)
                canScrollDebounced = true
            } else {
                canScrollDebounced = false
            }
        }
        LaunchedEffect(canScrollDebounced) {
            if (canScrollDebounced) buttonLocked = true
        }

        // 按钮：用户上划了 或 内容填满后锁定
        val showButton = (userScrolledUp || buttonLocked) && !followLocked

        // 自动跟随：followLocked 优先（按钮/手动滚回底部），否则 contentFilled 或 userScrolledUp 时停止
        LaunchedEffect(lastMsg?.content?.length, lastMsg?.status) {
            if (isStreaming && messages.isNotEmpty() && (followLocked || (!userScrolledUp && !contentFilled))) {
                programmaticScroll = true
                listState.scrollToItem(messages.lastIndex, Int.MAX_VALUE)
                programmaticScroll = false
            }
        }
        // 新消息重置
        LaunchedEffect(messages.size) {
            userScrolledUp = false
            followLocked = false
            contentFilled = false
            canScrollDebounced = false
            buttonLocked = false
            if (messages.isNotEmpty()) {
                programmaticScroll = true
                listState.scrollToItem(messages.lastIndex, Int.MAX_VALUE)
                programmaticScroll = false
            }
        }

        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp)
            ) {
                items(
                    count = messages.size,
                    key = { messages[it].id },
                    contentType = { messages[it].contentType.name }
                ) { idx ->
                    val msg = messages[idx]
                    val prev = if (idx > 0) messages[idx - 1].role else null
                    val next = if (idx < messages.lastIndex) messages[idx + 1].role else null
                    ChatBubble(msg, prev, next, ctx, onRegenerate, onStop, genHint)
                }
            }
            // "回到底部"悬浮按钮
            AnimatedVisibility(
                visible = showButton,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        followLocked = true
                        userScrolledUp = false
                        contentFilled = false
                        canScrollDebounced = false
                        buttonLocked = false
                        coroutineScope.launch {
                            programmaticScroll = true
                            listState.scrollToItem(messages.lastIndex, Int.MAX_VALUE)
                            programmaticScroll = false
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(50),
                    elevation = FloatingActionButtonDefaults.elevation(2.dp, 2.dp)
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, "回到底部")
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: Message, prev: MessageRole?, next: MessageRole?, ctx: Context, onRegenerate: () -> Unit = {}, onStop: () -> Unit = {}, genHint: String = "") {
    val isUser = msg.role == MessageRole.USER
    val streaming = msg.status == MessageStatus.STREAMING
    val first = prev != msg.role
    val last = next != msg.role
    val images = msg.allImageUrls
    val isAiImageMsg = !isUser && msg.contentType == com.aiaggregator.app.data.model.ContentType.IMAGE && (images.isNotEmpty() || streaming)

    // ── AI 图片消息：图片独立，不在气泡里 ──
    if (isAiImageMsg) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            // 生成中：显示加载动画
            if (streaming && msg.content.isEmpty()) {
                if (msg.contentType == com.aiaggregator.app.data.model.ContentType.IMAGE) {
                    ImageShimmer(
                        hint = genHint.ifBlank { "正在生成图片..." },
                        startedAt = msg.timestamp,
                        onStop = onStop
                    )
                } else {
                    TypingDots("正在连接模型，等待首个回复...")
                }
            }
            // 图片（无容器，直接展示）
            if (!streaming) {
                var showFull by remember { mutableStateOf(false) }
                var fullIndex by remember { mutableStateOf(0) }
                if (images.size == 1) {
                    AiImageCard(url = images[0],
                        onClick = { fullIndex = 0; showFull = true },
                        onRegenerate = onRegenerate,
                        onShare = { shareContent(ctx, images[0], true) },
                        onSave = { saveImageToDevice(ctx, images[0]) },
                        onCopy = { copyImageReference(ctx, images[0]) })
                } else {
                    ImageGallery(images = images,
                        onImageClick = { idx -> fullIndex = idx; showFull = true },
                        onRegenerate = onRegenerate,
                        onShare = { url -> shareContent(ctx, url, true) },
                        onSave = { url -> saveImageToDevice(ctx, url) },
                        onCopy = { url -> copyImageReference(ctx, url) })
                }
                if (showFull) {
                    FullscreenImageViewer(images = images, initialIndex = fullIndex,
                        onDismiss = { showFull = false },
                        onDownload = { url -> saveImageToDevice(ctx, url); Toast.makeText(ctx, "正在保存...", Toast.LENGTH_SHORT).show() },
                        onShare = { url -> shareContent(ctx, url, true) })
                }
            }
            // 模型信息（透明，无背景）
            if (last && (msg.modelName != null || msg.tokenCount != null)) {
                Spacer(Modifier.height(4.dp))
                Text(buildString {
                    if (msg.modelName != null) append("🤖 ${msg.modelName}")
                    if (msg.promptTokens != null && msg.completionTokens != null) {
                        if (msg.modelName != null) append(" · ")
                        append("↑${msg.promptTokens} ↓${msg.completionTokens}")
                    }
                }, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            }
        }
        return
    }

    // ── 用户图片消息：图片独立，文字气泡在下 ──
    if (isUser && images.isNotEmpty()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End) {
            var showFull by remember { mutableStateOf(false) }
            var fullIndex by remember { mutableStateOf(0) }
            // 图片独立展示（右对齐，无容器）
            ImageGrid(images = images, modifier = Modifier,
                onImageClick = { idx -> fullIndex = idx; showFull = true },
                onDownload = { url -> saveImageToDevice(ctx, url) })
            if (showFull) {
                FullscreenImageViewer(images = images, initialIndex = fullIndex,
                    onDismiss = { showFull = false },
                    onDownload = { url -> saveImageToDevice(ctx, url); Toast.makeText(ctx, "正在保存...", Toast.LENGTH_SHORT).show() },
                    onShare = { url -> shareContent(ctx, url, true) })
            }
            // 文字气泡（极淡，右对齐）
            if (msg.content.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(msg.content, Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        return
    }

    // ── AI 文字消息：无气泡，文字直接展示，几乎占满宽度 ──
    if (!isUser) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = if (last) 10.dp else 4.dp)
        ) {
            // 打字动画
            if (streaming && msg.content.isEmpty()) {
                TypingDots("正在连接模型，等待首个回复...")
            }
            // 文字内容（无容器，直接在背景上，按块渲染）
            if (msg.content.isBlank() && !streaming) {
                Text(
                    "⚠️ 未收到可显示内容，请点击重新生成或切换模型后重试。",
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                )
            }
            if (msg.content.isNotBlank()) {
                val hColor = MaterialTheme.colorScheme.primary
                val qColor = MaterialTheme.colorScheme.onSurfaceVariant
                val cColor = MaterialTheme.colorScheme.secondary
                val bodyFontSize = MaterialTheme.typography.bodyLarge.fontSize
                val blocks = remember(msg.id, msg.content) { parseMarkdownBlocks(msg.content) }
                blocks.forEachIndexed { blockIdx, block ->
                    when (block) {
                        is MarkdownBlock.CodeBlock -> CodeBlockView(block.language, block.code)
                        is MarkdownBlock.TableBlock -> TableView(block.headers, block.rows)
                        is MarkdownBlock.ImageBlock -> MarkdownImageView(block.url, block.alt, ctx)
                        is MarkdownBlock.AlertBlock -> AlertBlockView(block.type, block.lines, hColor, qColor, cColor)
                        is MarkdownBlock.DetailsBlock -> DetailsBlockView(block.title, block.lines, hColor, qColor, cColor)
                        is MarkdownBlock.ListBlock -> ListBlockView(block.items, hColor, qColor, cColor)
                        is MarkdownBlock.TextBlock -> {
                            val lines = block.lines.toMutableList()
                            if (streaming && blockIdx == blocks.lastIndex) {
                                lines[lines.lastIndex] = lines.last() + "▌"
                            }
                            val blockText = lines.joinToString("\n")
                            val annotated = remember(blockText, hColor, qColor, cColor, bodyFontSize) {
                                parseMarkdown(blockText, hColor, qColor, cColor, bodyFontSize)
                            }
                            val textStyle = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = bodyFontSize * 1.65
                            )
                            var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                            Text(
                                annotated,
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .pointerInput(annotated) {
                                        detectTapGestures { offset ->
                                            textLayoutResult?.let { layout ->
                                                val pos = layout.getOffsetForPosition(offset)
                                                annotated.getStringAnnotations("URL", pos, pos).firstOrNull()?.let {
                                                    openUrlSafely(ctx, it.item)
                                                }
                                            }
                                        }
                                    },
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                onTextLayout = { textLayoutResult = it }
                            )
                        }
                    }
                }
                // 流式光标：如果最后一个 block 不是 TextBlock（如代码块/表格），在末尾显示
                if (streaming && blocks.isNotEmpty() && blocks.last() !is MarkdownBlock.TextBlock) {
                    Text("▌", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            // 模型信息 + 操作按钮（极淡，无背景）
            if (last) {
                if (msg.modelName != null || msg.tokenCount != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(buildString {
                        if (msg.modelName != null) append("${msg.modelName}")
                        if (msg.promptTokens != null && msg.completionTokens != null) {
                            if (msg.modelName != null) append("  ·  ")
                            append("↑${msg.promptTokens} ↓${msg.completionTokens}")
                        }
                    }, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionText(Icons.Filled.Refresh, "重新生成", onRegenerate)
                    ActionText(Icons.Filled.Share, "分享") { shareContent(ctx, msg.content, false) }
                    ActionText(Icons.Filled.ContentCopy, "复制") { copyText(ctx, msg.content) }
                }
            }
        }
        return
    }

    // ── 用户文字消息：极淡圆角气泡，右对齐 ──
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = if (last) 8.dp else 2.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ) {
            Text(
                msg.content,
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ImageShimmer(hint: String? = null, startedAt: Long = System.currentTimeMillis(), onStop: (() -> Unit)? = null) {
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

    var now by remember(startedAt) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }
    val elapsedSec = ((now - startedAt).coerceAtLeast(0L) / 1000L).toInt()
    val progress = (elapsedSec / 240f).coerceIn(0.08f, 0.92f)

    // Smart hints — comfort first, warn later
    val displayHint = hint?.takeIf { it.isNotBlank() } ?: when {
        elapsedSec < 10 -> "正在连接服务器..."
        elapsedSec < 25 -> "正在生成，请稍候..."
        elapsedSec < 45 -> "模型处理中，不同平台速度有差异..."
        elapsedSec < 70 -> "中转站/逆向/官转等接口类型会影响生成速度"
        elapsedSec < 95 -> "上游服务器当前负载也会影响响应时间"
        elapsedSec < 120 -> "图片越复杂、尺寸越大，生成越慢"
        elapsedSec < 180 -> "已等待 2 分钟，可继续等待或停止重试"
        elapsedSec < 300 -> "仍在等待上游返回，请保持网络稳定"
        else -> "已超过 5 分钟，任务可能被系统或网络中断，建议停止后重新生成"
    }

    val hintColor = when {
        elapsedSec < 300 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
    }

    val elapsedText = when {
        elapsedSec < 60 -> "${elapsedSec}秒"
        else -> "${elapsedSec / 60}分${elapsedSec % 60}秒"
    }

    Surface(
        Modifier.widthIn(max = 300.dp).padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Shimmer placeholder
            Box(
                Modifier.fillMaxWidth().height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            // Hint text
            Text(displayHint, style = MaterialTheme.typography.labelMedium,
                color = hintColor, modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            // Elapsed time
            Text(elapsedText, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            // Stop button (appears after 2 min)
            if (onStop != null && elapsedSec >= 120) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onStop, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    Icon(Icons.Filled.Close, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("停止等待", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ── Attachment thumbnail for input preview ──

@Composable
private fun AttachmentThumb(uri: Uri, onRemove: () -> Unit) {
    val ctx = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
            try {
                val input = ctx.contentResolver.openInputStream(uri)
                if (input != null) { bitmap = android.graphics.BitmapFactory.decodeStream(input, null, opts); input.close() }
            } catch (_: Exception) {}
            if (bitmap == null) {
                try {
                    val fd = ctx.contentResolver.openFileDescriptor(uri, "r")
                    if (fd != null) { bitmap = android.graphics.BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, opts); fd.close() }
                } catch (_: Exception) {}
            }
            if (bitmap == null) {
                try {
                    val afd = ctx.contentResolver.openAssetFileDescriptor(uri, "r")
                    if (afd != null) { bitmap = android.graphics.BitmapFactory.decodeStream(afd.createInputStream(), null, opts); afd.close() }
                } catch (_: Exception) {}
            }
        }
    }
    Box(Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))) {
        val bmp = bitmap
        if (bmp != null) {
            Image(bitmap = bmp.asImageBitmap(), contentDescription = "预览",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }
        // × remove button
        Box(
            Modifier.align(Alignment.TopEnd).size(18.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(bottomStart = 8.dp))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, "移除", Modifier.size(12.dp), tint = Color.White)
        }
    }
}

@Composable
private fun AttachmentAddTile(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Add,
                "继续添加",
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }
    }
}

// ── Reliable local/remote thumbnail loader ──

@Composable
private fun ThumbImage(url: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val isLocal = url.startsWith("content://") || url.startsWith("file://")
    if (isLocal) {
        var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
        LaunchedEffect(url) {
            withContext(Dispatchers.IO) {
                val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                val uri = android.net.Uri.parse(url)
                try {
                    val input = ctx.contentResolver.openInputStream(uri)
                    if (input != null) { bitmap = android.graphics.BitmapFactory.decodeStream(input, null, opts); input.close() }
                } catch (_: Exception) {}
                if (bitmap == null) {
                    try {
                        val fd = ctx.contentResolver.openFileDescriptor(uri, "r")
                        if (fd != null) { bitmap = android.graphics.BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, opts); fd.close() }
                    } catch (_: Exception) {}
                }
            }
        }
        val bmp = bitmap
        if (bmp != null) {
            Image(bitmap = bmp.asImageBitmap(), contentDescription = null,
                modifier = modifier, contentScale = ContentScale.Crop)
        } else {
            Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
        }
    } else {
        AsyncImage(model = url, contentDescription = null,
            modifier = modifier, contentScale = ContentScale.Crop,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)))
    }
}

// ── User image grid (九宫格, WeChat style) ──

@Composable
private fun ImageGrid(images: List<String>, modifier: Modifier = Modifier, onImageClick: (Int) -> Unit, onDownload: (String) -> Unit) {
    val n = images.size
    val cellSize = 80.dp
    val gap = 4.dp
    val columns = when { n == 1 -> 1; n == 2 -> 2; n == 3 -> 3; n == 4 -> 2; else -> 3 }
    val width = cellSize * columns + gap * (columns - 1)
    Column(modifier.width(width)) {
        val rows = images.chunked(columns)
        rows.forEach { rowImages ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                rowImages.forEachIndexed { idx, url ->
                    val globalIdx = images.indexOf(url)
                    Box(Modifier.size(cellSize).clip(RoundedCornerShape(8.dp))) {
                        ThumbImage(url = url, modifier = Modifier.fillMaxSize().clickable { onImageClick(globalIdx) })
                    }
                }
                // Fill empty slots
                repeat(columns - rowImages.size) {
                    Spacer(Modifier.size(cellSize))
                }
                Spacer(Modifier.height(gap))
            }
        }
    }
}

// ── AI single image card (full-width) ──

/** 极简操作按钮：图标+文字，无背景容器 */
@Composable
private fun ActionText(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.clickable(onClick = onClick).padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

/** 单张 AI 图片：无容器，图片填满，下方独立操作行 */
@Composable
private fun AiImageCard(
    url: String, modifier: Modifier = Modifier,
    onClick: () -> Unit, onRegenerate: () -> Unit,
    onShare: () -> Unit, onSave: () -> Unit, onCopy: () -> Unit
) {
    Column(modifier) {
        AsyncImage(
            model = android.net.Uri.parse(url), contentDescription = "生成的图片",
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() },
            contentScale = ContentScale.FillWidth,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
        )
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionText(Icons.Filled.Refresh, "重新生成", onRegenerate)
            ActionText(Icons.Filled.Share, "分享", onShare)
            ActionText(Icons.Filled.Download, "保存", onSave)
            ActionText(Icons.Filled.ContentCopy, "复制", onCopy)
        }
    }
}

/** 多张 AI 图片：无容器横向流式，每张独立操作行 */
@Composable
private fun ImageGallery(
    images: List<String>, modifier: Modifier = Modifier,
    onImageClick: (Int) -> Unit,
    onRegenerate: () -> Unit, onShare: (String) -> Unit,
    onSave: (String) -> Unit, onCopy: (String) -> Unit
) {
    Column(modifier) {
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(images.size) { idx ->
                val url = images[idx]
                Column {
                    AsyncImage(
                        model = android.net.Uri.parse(url), contentDescription = "图片 $idx",
                        modifier = Modifier.sizeIn(maxWidth = 260.dp, maxHeight = 260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(idx) },
                        contentScale = ContentScale.Fit,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionText(Icons.Filled.Refresh, "重新生成", onRegenerate)
                        ActionText(Icons.Filled.Share, "分享") { onShare(url) }
                        ActionText(Icons.Filled.Download, "保存") { onSave(url) }
                        ActionText(Icons.Filled.ContentCopy, "复制") { onCopy(url) }
                    }
                }
            }
        }
    }
}

// ── Fullscreen viewer with swipe ──

@Composable
private fun FullscreenImageViewer(images: List<String>, initialIndex: Int, onDismiss: () -> Unit, onDownload: (String) -> Unit, onShare: (String) -> Unit) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            val currentUrl = images.getOrNull(currentIndex) ?: return@Box
            AsyncImage(
                model = android.net.Uri.parse(currentUrl), contentDescription = "全屏查看",
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }.graphicsLayer {
                    scaleX = scale; scaleY = scale
                    translationX = offsetX; translationY = offsetY
                }.clickable { onDismiss() },
                placeholder = ColorPainter(Color.DarkGray)
            )
            // Top bar
            Row(Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 16.dp)) {
                // Page indicator
                if (images.size > 1) {
                    Text("${currentIndex + 1}/${images.size}",
                        Modifier.align(Alignment.CenterVertically).padding(end = 12.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f))
                }
                IconButton(onClick = { onShare(currentUrl) }) {
                    Icon(Icons.Filled.Share, "分享", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { onDownload(currentUrl) }) {
                    Icon(Icons.Filled.Download, "保存", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "关闭", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
                }
            }
            // Prev / Next arrows
            if (images.size > 1) {
                if (currentIndex > 0) {
                    Box(
                        Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
                            .size(36.dp).clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { currentIndex--; scale = 1f; offsetX = 0f; offsetY = 0f },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "上一张", Modifier.size(20.dp), tint = Color.White)
                    }
                }
                if (currentIndex < images.size - 1) {
                    Box(
                        Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                            .size(36.dp).clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { currentIndex++; scale = 1f; offsetX = 0f; offsetY = 0f },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "下一张", Modifier.size(20.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingDots(message: String = "正在等待模型回复...") {
    val infinite = rememberInfiniteTransition()
    Column(
        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
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
        Text(
            message,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
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

private fun copyImageReference(ctx: Context, imageRef: String) {
    val scheme = runCatching { Uri.parse(imageRef).scheme?.lowercase() }.getOrNull()
    if (scheme == "content" || scheme == "file") {
        Toast.makeText(ctx, "这是本地图片，请使用分享发送图片；公开链接需配置图床/OSS", Toast.LENGTH_LONG).show()
        return
    }
    copyText(ctx, imageRef)
}

private fun openUrlSafely(ctx: Context, rawUrl: String) {
    val url = rawUrl.trim()
    val uri = try { android.net.Uri.parse(url) } catch (_: Exception) { null }
    val scheme = uri?.scheme?.lowercase()
    if (uri == null || scheme !in setOf("http", "https", "mailto", "tel")) {
        copyText(ctx, rawUrl)
        Toast.makeText(ctx, "无法打开链接，已复制", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        ctx.safeStartActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK), "无法打开链接")
    } catch (_: Exception) {
        copyText(ctx, rawUrl)
        Toast.makeText(ctx, "无法打开链接，已复制", Toast.LENGTH_SHORT).show()
    }
}

private fun shareContent(ctx: Context, text: String, isImage: Boolean) {
    if (isImage) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val uri = ImageStorageManager.toShareUri(ctx, text)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (uri == null) {
                    Toast.makeText(ctx, "图片无法分享，请先保存后重试", Toast.LENGTH_SHORT).show()
                    return@withContext
                }
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.safeStartActivity(android.content.Intent.createChooser(intent, "分享到"), "未找到可分享图片的应用")
            }
        }
        return
    }

    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    ctx.safeStartActivity(android.content.Intent.createChooser(intent, "分享到"), "未找到可分享的应用")
}

// ── Markdown block-level rendering ──

private sealed class MarkdownBlock {
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class TextBlock(val lines: List<String>) : MarkdownBlock()
    data class ImageBlock(val url: String, val alt: String = "", val title: String = "") : MarkdownBlock()
    data class AlertBlock(val type: String, val lines: List<String>) : MarkdownBlock()
    data class DetailsBlock(val title: String, val lines: List<String>) : MarkdownBlock()
    data class ListBlock(val items: List<ListItem>) : MarkdownBlock()
}

private data class ListItem(
    val depth: Int,
    val marker: String,
    val text: String,
    val checked: Boolean? = null
)

private val LIST_LINE_REGEX = Regex("^(\\s*)([-*+]\\s+(?:\\[[ xX]]\\s+)?|\\d+\\.\\s+)(.+)$")

private fun splitLooseTableRow(line: String): List<String>? {
    val trimmed = line.trim()
    if (trimmed.isBlank()) return null
    val parts = if (trimmed.contains('\t')) {
        trimmed.split('\t')
    } else {
        trimmed.split(Regex("\\s{2,}"))
    }.map { it.trim() }.filter { it.isNotEmpty() }
    return parts.takeIf { it.size >= 2 }
}

private fun parseListItem(line: String): ListItem? {
    val match = LIST_LINE_REGEX.matchEntire(line) ?: return null
    val indent = match.groupValues[1].replace("\t", "    ").length
    val rawMarker = match.groupValues[2].trim()
    val text = match.groupValues[3]
    val checked = when {
        rawMarker.contains("[x]", ignoreCase = true) -> true
        rawMarker.contains("[ ]") -> false
        else -> null
    }
    val marker = when {
        checked == true -> "✓"
        checked == false -> "☐"
        rawMarker.firstOrNull()?.isDigit() == true -> rawMarker.substringBefore('.') + "."
        else -> "•"
    }
    return ListItem(depth = indent / 2, marker = marker, text = text, checked = checked)
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val result = mutableListOf<MarkdownBlock>()
    val lines = text.split('\n')
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trimStart()

        // ── Code block ──
        if (trimmed.startsWith("```")) {
            val lang = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            result.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            if (i < lines.size) i++ // skip closing ```
            continue
        }

        // ── Alert block: > [!WARNING] / > [!ERROR] / > [!INFO] / > [!SUCCESS] ──
        val alertMatch = Regex("^>\\s*\\[!(WARNING|ERROR|INFO|SUCCESS|NOTE)]\\s*$", RegexOption.IGNORE_CASE).matchEntire(trimmed)
        if (alertMatch != null) {
            val type = alertMatch.groupValues[1].uppercase()
            val alertLines = mutableListOf<String>()
            i++
            while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                alertLines.add(lines[i].trimStart().removePrefix(">").trimStart())
                i++
            }
            result.add(MarkdownBlock.AlertBlock(type, alertLines))
            continue
        }

        // ── Details block: <details><summary>标题</summary>... ──
        if (trimmed.startsWith("<details", ignoreCase = true)) {
            val detailsLines = mutableListOf<String>()
            var title = "点击展开查看"
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("</details>", ignoreCase = true)) {
                val current = lines[i].trim()
                val summary = Regex("<summary>(.*?)</summary>", RegexOption.IGNORE_CASE).find(current)
                if (summary != null) title = summary.groupValues[1].ifBlank { title }
                else detailsLines.add(lines[i])
                i++
            }
            if (i < lines.size) i++
            result.add(MarkdownBlock.DetailsBlock(title, detailsLines))
            continue
        }

        // ── Table block (consecutive lines starting with |) ──
        if (trimmed.startsWith("|")) {
            val tableLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                tableLines.add(lines[i])
                i++
            }
            if (tableLines.isNotEmpty()) {
                val dataRows = tableLines
                    .filter { !it.trim().matches(Regex("^\\|[-\\s|]+\\|$")) } // skip separator
                    .map { row -> row.trim().trim('|').split('|').map { it.trim() } }
                if (dataRows.isNotEmpty()) {
                    result.add(MarkdownBlock.TableBlock(dataRows.first(), dataRows.drop(1)))
                }
            }
            continue
        }

        // ── Loose table block: tab-separated or multi-space aligned rows ──
        splitLooseTableRow(line)?.let { firstRow ->
            val tableRows = mutableListOf(firstRow)
            var j = i + 1
            while (j < lines.size) {
                val row = splitLooseTableRow(lines[j]) ?: break
                val compatible = row.size == firstRow.size || kotlin.math.abs(row.size - firstRow.size) <= 1
                if (!compatible) break
                tableRows.add(row)
                j++
            }
            if (tableRows.size >= 2) {
                result.add(MarkdownBlock.TableBlock(tableRows.first(), tableRows.drop(1)))
                i = j
                continue
            }
        }

        // ── Nested list block ──
        parseListItem(line)?.let { firstItem ->
            val items = mutableListOf(firstItem)
            i++
            while (i < lines.size) {
                val item = parseListItem(lines[i]) ?: break
                items.add(item)
                i++
            }
            result.add(MarkdownBlock.ListBlock(items))
            continue
        }

        // ── Text block (consecutive non-special lines) ──
        if (trimmed.isNotBlank()) {
            val textLines = mutableListOf<String>()
            while (i < lines.size) {
                val l = lines[i].trimStart()
                if (l.isBlank() || l.startsWith("```") || l.startsWith("|") || parseListItem(lines[i]) != null) break
                textLines.add(lines[i])
                i++
            }
            // Split text block: extract markdown image syntax or standalone image URLs as separate ImageBlocks
            val imageRegex = Regex("https?://[^\\s)\\]>，。、！？]+\\.(jpg|jpeg|png|gif|webp|bmp|svg)(\\?[^\\s)]*)?", RegexOption.IGNORE_CASE)
            val markdownImageRegex = Regex("^!\\[([^]]*)]\\((\\S+?)(?:\\s+\"([^\"]*)\")?\\)$")
            val currentTextLines = mutableListOf<String>()
            for (line in textLines) {
                val trimmedLine = line.trim()
                val markdownImage = markdownImageRegex.matchEntire(trimmedLine)
                val imageUrl = imageRegex.find(trimmedLine)
                if (markdownImage != null) {
                    if (currentTextLines.isNotEmpty()) {
                        result.add(MarkdownBlock.TextBlock(currentTextLines.toList()))
                        currentTextLines.clear()
                    }
                    result.add(
                        MarkdownBlock.ImageBlock(
                            url = markdownImage.groupValues[2],
                            alt = markdownImage.groupValues[1],
                            title = markdownImage.groupValues.getOrNull(3).orEmpty()
                        )
                    )
                } else if (imageUrl != null && trimmedLine == imageUrl.value) {
                    if (currentTextLines.isNotEmpty()) {
                        result.add(MarkdownBlock.TextBlock(currentTextLines.toList()))
                        currentTextLines.clear()
                    }
                    result.add(MarkdownBlock.ImageBlock(imageUrl.value))
                } else {
                    currentTextLines.add(line)
                }
            }
            if (currentTextLines.isNotEmpty()) {
                result.add(MarkdownBlock.TextBlock(currentTextLines))
            }
            continue
        }

        // ── Blank line: skip ──
        i++
    }

    return result
}

@Composable
private fun AlertBlockView(type: String, lines: List<String>, hColor: Color, qColor: Color, cColor: Color) {
    val normalizedType = type.uppercase()
    val colors = when (normalizedType) {
        "ERROR" -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
        "WARNING" -> Color(0xFFB26A00) to Color(0xFFFFF4D6)
        "SUCCESS" -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        else -> hColor to MaterialTheme.colorScheme.primaryContainer
    }
    val label = when (normalizedType) {
        "ERROR" -> "错误"
        "WARNING" -> "注意"
        "SUCCESS" -> "完成"
        else -> "说明"
    }
    Surface(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        color = colors.second.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, colors.first.copy(alpha = 0.45f))
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.first, fontWeight = FontWeight.SemiBold)
            if (lines.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    parseMarkdown(lines.joinToString("\n"), hColor, qColor, cColor, MaterialTheme.typography.bodyMedium.fontSize),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.55),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DetailsBlockView(title: String, lines: List<String>, hColor: Color, qColor: Color, cColor: Color) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (expanded) "▼" else "▶",
                    Modifier.width(22.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            if (expanded && lines.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Text(
                    parseMarkdown(lines.joinToString("\n"), hColor, qColor, cColor, MaterialTheme.typography.bodyMedium.fontSize),
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.55),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ListBlockView(items: List<ListItem>, hColor: Color, qColor: Color, cColor: Color) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        items.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = (item.depth * 18).dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    item.marker,
                    Modifier.width(28.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.checked == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    parseMarkdown(item.text, hColor, qColor, cColor, MaterialTheme.typography.bodyLarge.fontSize),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.65),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MarkdownImageView(url: String, alt: String, ctx: Context) {
    var showFull by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }

    if (loadFailed) {
        Surface(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("图片加载失败", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                ActionText(Icons.Filled.ContentCopy, "复制链接") { copyText(ctx, url) }
            }
        }
        return
    }

    AsyncImage(
        model = url,
        contentDescription = alt.ifBlank { "图片" },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showFull = true },
        contentScale = ContentScale.Fit,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
        onError = { loadFailed = true }
    )

    if (showFull) {
        FullscreenImageViewer(
            images = listOf(url),
            initialIndex = 0,
            onDismiss = { showFull = false },
            onDownload = { saveImageToDevice(ctx, it); Toast.makeText(ctx, "正在保存...", Toast.LENGTH_SHORT).show() },
            onShare = { shareContent(ctx, it, true) }
        )
    }
}

@Composable
private fun CodeBlockView(language: String, code: String) {
    val ctx = LocalContext.current
    val lineCount = code.lines().size
    val maxCollapsedLines = 10
    var expanded by remember { mutableStateOf(false) }
    var showFull by remember { mutableStateOf(false) }
    val needsCollapse = lineCount > maxCollapsedLines

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
    ) {
        // ── 顶部栏：语言标签 + 操作按钮 ──
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(language.ifBlank { "code" }, style = MaterialTheme.typography.labelSmall, color = Color(0xFF757575))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // 全屏按钮
                IconButton(onClick = { showFull = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Fullscreen, "全屏", modifier = Modifier.size(14.dp), tint = Color(0xFF757575))
                }
                // 下载按钮
                IconButton(onClick = {
                    val ext = when (language.lowercase()) {
                        "python", "py" -> "py"; "javascript", "js" -> "js"; "kotlin", "kt" -> "kt"
                        "java" -> "java"; "html" -> "html"; "css" -> "css"; "json" -> "json"
                        "xml" -> "xml"; "bash", "sh", "shell" -> "sh"; "sql" -> "sql"
                        "typescript", "ts" -> "ts"; "c" -> "c"; "cpp", "c++" -> "cpp"
                        "go" -> "go"; "rust", "rs" -> "rs"; "swift" -> "swift"
                        else -> "txt"
                    }
                    val file = java.io.File(ctx.cacheDir, "code.$ext")
                    file.writeText(code)
                    Toast.makeText(ctx, "已保存到 ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Download, "下载", modifier = Modifier.size(14.dp), tint = Color(0xFF757575))
                }
                // 复制按钮
                IconButton(onClick = {
                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("code", code))
                    Toast.makeText(ctx, "已复制", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.ContentCopy, "复制", modifier = Modifier.size(14.dp), tint = Color(0xFF757575))
                }
            }
        }

        // ── 代码内容 ──
        Box {
            val maxH = if (needsCollapse && !expanded) (maxCollapsedLines * 22).dp else Dp.Unspecified
            val highlighted = highlightCode(code, language)
            Text(
                highlighted,
                Modifier
                    .then(if (maxH != Dp.Unspecified) Modifier.heightIn(max = maxH) else Modifier)
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5
                ),
                softWrap = false
            )
            // 底部渐变遮罩（折叠状态）
            if (needsCollapse && !expanded) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x00F8FAFC), Color(0xFFF8FAFC))
                            )
                        )
                )
            }
        }

        // ── 查看全部 / 收起 按钮 ──
        if (needsCollapse) {
            Text(
                if (expanded) "收起" else "查看全部（共 $lineCount 行）",
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showFull) {
        Popup(onDismissRequest = { showFull = false }) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(language.ifBlank { "code" }, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {
                            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("code", code))
                            Toast.makeText(ctx, "已复制", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.ContentCopy, "复制", modifier = Modifier.size(18.dp), tint = Color(0xFF757575))
                        }
                        IconButton(onClick = { showFull = false }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, "关闭", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                HorizontalDivider()
                Text(
                    highlightCode(code, language),
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5
                    ),
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun TableView(headers: List<String>, rows: List<List<String>>) {
    val ctx = LocalContext.current
    val colCount = maxOf(headers.size, rows.maxOfOrNull { it.size } ?: 0, 1)
    val normalizedHeaders = headers + List((colCount - headers.size).coerceAtLeast(0)) { "" }
    val columnWidths = remember(headers, rows) {
        List(colCount) { col ->
            val maxLen = maxOf(
                normalizedHeaders.getOrNull(col)?.length ?: 0,
                rows.maxOfOrNull { it.getOrNull(col)?.length ?: 0 } ?: 0
            )
            ((maxLen.coerceIn(4, 36) * 8) + 48).dp
        }
    }
    var showFull by remember { mutableStateOf(false) }
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    fun isNumericCell(value: String): Boolean = value.trim().matches(Regex("^[+-]?[0-9][0-9,]*(\\.[0-9]+)?%?$|^\\([0-9, .-]+\\)$"))

    // ── 表格本体（可复用） ──
    @Composable
    fun TableContent(modifier: Modifier = Modifier, compact: Boolean = true) {
        Column(modifier.horizontalScroll(rememberScrollState())) {
            // 表头
            Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                normalizedHeaders.forEachIndexed { colIdx, h ->
                    Box(
                        Modifier
                            .width(columnWidths[colIdx])
                            .height(40.dp)
                            .border(0.5.dp, borderColor.copy(alpha = 0.35f))
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(h.trim(),
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            softWrap = false,
                            maxLines = 1,
                            textAlign = TextAlign.Center)
                    }
                }
            }
            // 数据行
            rows.forEachIndexed { rowIdx, row ->
                val bg = if (rowIdx % 2 == 0) Color.White else Color(0xFFF8F8F8)
                Row {
                    val padded = row + List((colCount - row.size).coerceAtLeast(0)) { "" }
                    padded.take(colCount).forEachIndexed { colIdx, cell ->
                        Box(
                            Modifier
                                .width(columnWidths[colIdx])
                                .then(if (compact) Modifier.height(40.dp) else Modifier.heightIn(min = 40.dp))
                                .border(0.5.dp, borderColor.copy(alpha = 0.28f))
                                .background(bg),
                            contentAlignment = if (isNumericCell(cell)) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Text(cell.trim(),
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = !compact,
                                maxLines = if (compact) 1 else Int.MAX_VALUE,
                                overflow = if (compact) TextOverflow.Ellipsis else TextOverflow.Clip,
                                textAlign = if (isNumericCell(cell)) TextAlign.End else TextAlign.Start)
                        }
                    }
                }
            }
        }
    }

    // ── 表格卡片 ──
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
    ) {
        val maxTableRows = 8
        val needsCollapse = rows.size > maxTableRows

        Column {
            // 表格内容（带最大高度限制）
            Box(
                Modifier.then(
                    if (needsCollapse && !showFull) Modifier.heightIn(max = ((maxTableRows + 1) * 40).dp)
                    else Modifier
                )
            ) {
                TableContent(Modifier.fillMaxWidth())
                // 底部渐变遮罩
                if (needsCollapse && !showFull) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Brush.verticalGradient(listOf(Color(0x00FAFAFA), Color(0xFFFAFAFA))))
                    )
                }
            }
            // 底部操作栏：不遮挡表格内容
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (needsCollapse && !showFull) {
                    Text(
                        "查看全部（共 ${rows.size} 行）",
                        Modifier.clickable { showFull = true },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionText(Icons.Filled.Fullscreen, "放大") { showFull = true }
                    ActionText(Icons.Filled.ContentCopy, "复制") {
                        val text = buildString {
                            appendLine(headers.joinToString("\t"))
                            rows.forEach { row -> appendLine(row.joinToString("\t")) }
                        }
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("table", text))
                        Toast.makeText(ctx, "已复制表格", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // ── 全屏覆盖层（Popup，脱离 LazyColumn 限制） ──
    if (showFull) {
        Popup(onDismissRequest = { showFull = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { showFull = false }
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { /* 拦截 */ }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("表格详情", style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { showFull = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, "关闭", modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider()
                    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        TableContent(Modifier.fillMaxWidth(), compact = false)
                    }
                }
            }
        }
    }
}

private fun parseMarkdown(text: String, hColor: Color, qColor: Color, cColor: Color, baseFontSize: TextUnit = 16.sp): androidx.compose.ui.text.AnnotatedString {
    val lines = text.split('\n')
    return buildAnnotatedString {
        lines.forEachIndexed { idx, line ->
            if (idx > 0) append('\n')
            val trimmed = line.trimStart()
            when {
                // ── Code block boundary (skip, handled by CodeBlockView) ──
                trimmed.startsWith("```") -> { /* no-op */ }
                // ── # H1 heading ──
                trimmed.startsWith("# ") -> {
                    withStyle(SpanStyle(fontSize = baseFontSize * 1.4, fontWeight = FontWeight.Bold, color = hColor)) {
                        appendInlineStyles(trimmed.removePrefix("# "))
                    }
                }
                // ── ## H2 heading ──
                trimmed.startsWith("## ") -> {
                    withStyle(SpanStyle(fontSize = baseFontSize * 1.25, fontWeight = FontWeight.Bold, color = hColor)) {
                        appendInlineStyles(trimmed.removePrefix("## "))
                    }
                }
                // ── ### H3 heading ──
                trimmed.startsWith("###") -> {
                    withStyle(SpanStyle(fontSize = baseFontSize * 1.1, fontWeight = FontWeight.Bold, color = hColor)) {
                        appendInlineStyles(trimmed.removePrefix("###").trimStart())
                    }
                }
                // ── > Blockquote ──
                trimmed.startsWith("> ") -> {
                    withStyle(SpanStyle(color = qColor, background = qColor.copy(alpha = 0.1f))) {
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
                // ── Blank line ──
                trimmed.isBlank() -> append(' ')
                // ── Regular text ──
                else -> appendInlineStyles(trimmed)
            }
        }
    }
}

private fun highlightCode(code: String, language: String): AnnotatedString {
    val keywords = setOf(
        "def", "class", "if", "else", "elif", "for", "while", "return", "import", "from",
        "fun", "val", "var", "when", "try", "catch", "finally", "throw", "new", "this",
        "super", "extends", "implements", "interface", "enum", "abstract", "static",
        "public", "private", "protected", "internal", "override", "open", "sealed",
        "data", "object", "companion", "suspend", "async", "await", "yield", "break",
        "continue", "pass", "in", "not", "and", "or", "is", "as", "null", "None",
        "True", "False", "true", "false", "nil", "let", "const", "function", "var",
        "switch", "case", "default", "do", "struct", "enum", "union", "typedef",
        "fn", "mut", "pub", "use", "mod", "impl", "trait", "where", "match", "loop",
        "select", "insert", "update", "delete", "create", "drop", "alter", "grant",
        "revoke", "commit", "rollback", "begin", "end", "declare", "set", "exec",
        "execute", "procedure", "trigger", "view", "index", "table", "column",
        "WHERE", "FROM", "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP",
        "ALTER", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "GROUP", "ORDER",
        "BY", "HAVING", "LIMIT", "OFFSET", "UNION", "ALL", "DISTINCT", "AS", "IN",
        "NOT", "AND", "OR", "IS", "NULL", "BETWEEN", "LIKE", "EXISTS", "INTO",
        "VALUES", "SET", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CASCADE",
        "CONSTRAINT", "INDEX", "VIEW", "TRIGGER", "PROCEDURE", "FUNCTION", "RETURNS"
    )

    val keywordColor = Color(0xFF7C3AED)
    val stringColor = Color(0xFF059669)
    val commentColor = Color(0xFF9CA3AF)
    val numberColor = Color(0xFFD97706)
    val defaultColor = Color(0xFF2D2D2D)

    return buildAnnotatedString {
        var i = 0
        while (i < code.length) {
            when {
                // Line comment: // or #
                code.startsWith("//", i) || (code[i] == '#' && (i == 0 || code[i - 1] != '#')) -> {
                    val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
                    withStyle(SpanStyle(color = commentColor)) { append(code.substring(i, end)) }
                    i = end
                }
                // Block comment: /* */
                code.startsWith("/*", i) -> {
                    val end = code.indexOf("*/", i + 2).let { if (it == -1) code.length else it + 2 }
                    withStyle(SpanStyle(color = commentColor)) { append(code.substring(i, end)) }
                    i = end
                }
                // Double-quoted string
                code[i] == '"' -> {
                    var end = i + 1
                    while (end < code.length && code[end] != '"') {
                        if (code[end] == '\\') end++ // skip escaped char
                        end++
                    }
                    if (end < code.length) end++ // include closing quote
                    withStyle(SpanStyle(color = stringColor)) { append(code.substring(i, end)) }
                    i = end
                }
                // Single-quoted string
                code[i] == '\'' -> {
                    var end = i + 1
                    while (end < code.length && code[end] != '\'') {
                        if (code[end] == '\\') end++
                        end++
                    }
                    if (end < code.length) end++
                    withStyle(SpanStyle(color = stringColor)) { append(code.substring(i, end)) }
                    i = end
                }
                // Number (digit or dot followed by digit)
                code[i].isDigit() || (code[i] == '.' && i + 1 < code.length && code[i + 1].isDigit()) -> {
                    var end = i
                    while (end < code.length && (code[end].isDigit() || code[end] == '.' || code[end] == 'x' || code[end] in 'a'..'f' || code[end] in 'A'..'F')) end++
                    withStyle(SpanStyle(color = numberColor)) { append(code.substring(i, end)) }
                    i = end
                }
                // Identifier / keyword
                code[i].isLetter() || code[i] == '_' -> {
                    var end = i
                    while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '_')) end++
                    val word = code.substring(i, end)
                    val color = if (word in keywords) keywordColor else defaultColor
                    withStyle(SpanStyle(color = color)) { append(word) }
                    i = end
                }
                else -> {
                    append(code[i])
                    i++
                }
            }
        }
    }
}

private val URL_REGEX = Regex("https?://[^\\s)\\]>，。、！？]+")

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
            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end > i) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            // Markdown 链接：[标题](URL)
            text.startsWith("[", i) -> {
                val labelEnd = text.indexOf("](", i + 1)
                val urlEnd = if (labelEnd > i) text.indexOf(")", labelEnd + 2) else -1
                if (labelEnd > i && urlEnd > labelEnd) {
                    val label = text.substring(i + 1, labelEnd)
                    val url = text.substring(labelEnd + 2, urlEnd).trim()
                    pushStringAnnotation("URL", url)
                    withStyle(SpanStyle(color = Color(0xFF2196F3), textDecoration = TextDecoration.Underline)) {
                        append(label.ifBlank { url })
                    }
                    pop()
                    i = urlEnd + 1
                } else { append(text[i]); i++ }
            }
            // URL 检测：蓝色下划线 + 注解（可点击）
            text.substring(i).let { sub ->
                val match = URL_REGEX.find(sub)
                match != null && match.range.first == 0
            } -> {
                val match = URL_REGEX.find(text.substring(i))!!
                val url = match.value
                pushStringAnnotation("URL", url)
                withStyle(SpanStyle(color = Color(0xFF2196F3), textDecoration = TextDecoration.Underline)) {
                    append(url)
                }
                pop()
                i += url.length
            }
            else -> { append(text[i]); i++ }
        }
    }
}

private fun detectImageExt(url: String, bytes: ByteArray): String = ImageStorageManager.detectImageExt(url, bytes)

private fun saveImageToDevice(ctx: Context, imageUrl: String) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val bytes = ImageStorageManager.readImageBytes(ctx, imageUrl) ?: throw IllegalStateException("无法读取图片")
            val ext = detectImageExt(imageUrl, bytes)
            val mimeType = when (ext) { "jpg" -> "image/jpeg"; "webp" -> "image/webp"; else -> "image/png" }
            val fileName = "AI_${System.currentTimeMillis()}.$ext"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, mimeType)
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
    attached: List<Uri>,
    onClearFile: (Int) -> Unit,
    onClearAllFiles: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickImages: () -> Unit,
    onPickFiles: () -> Unit,
    onStop: () -> Unit,
    onAddImages: (List<Uri>) -> Unit
) {
    val hasContent = text.isNotBlank() || attached.isNotEmpty()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val kbCtrl = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }
    var fullGallery by remember { mutableStateOf(false) }
    var selectedGalleryUris by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var galleryImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var galleryAlbums by remember { mutableStateOf<List<GalleryAlbum>>(emptyList()) }
    var selectedAlbum by remember { mutableStateOf<String?>(null) } // null = 全部
    var hasPermission by remember { mutableStateOf(false) }
    var inputFocused by remember { mutableStateOf(false) }

    // P1: 一开始就加载相册，不等展开
    LaunchedEffect(Unit) {
        val perm = if (android.os.Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES
                   else android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(ctx, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val (images, albums) = queryGalleryImages(ctx)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    galleryImages = images
                    galleryAlbums = albums
                }
            }
        }
    }

    // P1: 全屏相册时隐藏整个输入区域
    AnimatedVisibility(visible = !fullGallery) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
        // 生成进度条
        if (generating) {
            LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp), color = MaterialTheme.colorScheme.primary, trackColor = Color.Transparent)
        }

        // ── 模型选择（小芯片，无容器）──
        if (available.isNotEmpty() && !inputFocused) {
            var modelExpanded by remember { mutableStateOf(false) }
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)) {
                Surface(
                    Modifier.clickable { modelExpanded = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        val label = model?.let { m -> m.displayName.ifBlank { m.modelName } } ?: "选择模型"
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
                        Icon(Icons.Filled.ArrowDropDown, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            DropdownMenu(expanded = modelExpanded, onDismissRequest = { modelExpanded = false }) {
                available.groupBy { it.category }.forEach { (cat, ms) ->
                    DropdownMenuItem(text = { Text(cat.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }, onClick = {}, enabled = false)
                    ms.forEach { m ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(m.displayName.ifBlank { m.modelName }, style = MaterialTheme.typography.bodyMedium)
                                    if (m.displayName.isNotBlank()) Text(m.modelName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            trailingIcon = { if (m.id == model?.id) Icon(Icons.Filled.Check, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) },
                            onClick = { onSelect(m); modelExpanded = false }
                        )
                    }
                }
            }
        }

        // ── 附件预览 ──
        AnimatedVisibility(visible = attached.isNotEmpty()) {
            LazyRow(
                Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(attached.size, key = { attached[it] }) { idx ->
                    AttachmentThumb(uri = attached[idx], onRemove = { onClearFile(idx) })
                }
                item(key = "add_attachment_tile") {
                    AttachmentAddTile {
                        focusManager.clearFocus(force = true)
                        kbCtrl?.hide()
                        expanded = true
                    }
                }
            }
        }

        // ── 输入胶囊（独立悬浮）──
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text, onValueChange = onTextChange,
                    placeholder = { Text("输入消息...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                    modifier = Modifier.weight(1f)
                        .onFocusChanged {
                            inputFocused = it.isFocused
                            if (it.isFocused) expanded = false
                        },
                    maxLines = 5, singleLine = false,
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Default),
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )
                AnimatedVisibility(visible = generating) {
                    IconButton(onClick = onStop, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Filled.Stop, "停止", tint = MaterialTheme.colorScheme.error)
                    }
                }
                AnimatedVisibility(visible = !generating && hasContent) {
                    IconButton(onClick = onSend, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Send, "发送", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                AnimatedVisibility(visible = !generating && !hasContent) {
                    Row {
                        FilledTonalIconButton(
                            onClick = {
                                focusManager.clearFocus(force = true)
                                kbCtrl?.hide()
                                onTakePhoto()
                            },
                            modifier = Modifier.size(38.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Filled.CameraAlt, "拍照", Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = {
                                focusManager.clearFocus(force = true)
                                kbCtrl?.hide()
                                expanded = !expanded
                            },
                            modifier = Modifier.size(38.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (expanded) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                                contentColor = if (expanded) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                                if (expanded) "收起" else "展开",
                                Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── 展开内容（无容器，键盘弹出时自动收起）──
        AnimatedVisibility(visible = expanded && !fullGallery && !inputFocused) {
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(10.dp))
                // 功能按钮（等宽排列，占满一行）
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    listOf(
                        Triple(Icons.Filled.CameraAlt, "拍照", {
                            focusManager.clearFocus(force = true)
                            kbCtrl?.hide()
                            onTakePhoto(); expanded = false
                        }),
                        Triple(Icons.Filled.Collections, "图片", {
                            focusManager.clearFocus(force = true)
                            kbCtrl?.hide()
                            onPickImages(); expanded = false
                        }),
                        Triple(Icons.Filled.FolderOpen, "文件", {
                            focusManager.clearFocus(force = true)
                            kbCtrl?.hide()
                            onPickFiles(); expanded = false
                        })
                    ).forEachIndexed { index, (icon, label, action) ->
                        val colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        val (containerColor, contentColor) = colors[index]
                        Surface(
                            onClick = action as () -> Unit,
                            shape = RoundedCornerShape(20.dp),
                            color = containerColor.copy(alpha = 0.72f),
                            tonalElevation = 2.dp,
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier.size(38.dp).clip(RoundedCornerShape(14.dp)).background(contentColor.copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, label, Modifier.size(24.dp), tint = contentColor)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                // 相册预览（3 行，第 3 行截断，不可滚动，上滑进全屏）
                if (hasPermission && galleryImages.isNotEmpty()) {
                    Text("最近图片", Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val peekItems = 12 // 3 行 × 4 列
                    Box(
                        Modifier.fillMaxWidth().height(240.dp)
                            .clipToBounds() // 自然截断，无渐变遮罩
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    if (dragAmount < -40) {
                                        focusManager.clearFocus(force = true)
                                        kbCtrl?.hide()
                                        fullGallery = true
                                        expanded = false
                                    }
                                }
                            }
                    ) {
                        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp)) {
                            val items = galleryImages.take(peekItems)
                            for (rowStart in items.indices step 4) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    for (i in rowStart until minOf(rowStart + 4, items.size)) {
                                        Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(6.dp)).clickable {
                                            onAddImages(listOf(items[i])); expanded = false
                                        }) {
                                            AsyncImage(
                                                model = coil.request.ImageRequest.Builder(ctx).data(items[i]).size(200).build(),
                                                contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(3.dp))
                            }
                        }
                    }
                } else if (!hasPermission) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("相册权限未授权", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("可通过上方「图片」按钮选择图片", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
    } // AnimatedVisibility(!fullGallery)

    // ── 全屏相册（Dialog 渲染在所有内容之上，真正全屏）──
    if (fullGallery) {
        Dialog(
            onDismissRequest = { fullGallery = false; selectedGalleryUris = emptySet(); selectedAlbum = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    // 顶部栏
                    Column(
                        Modifier.fillMaxWidth()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    if (dragAmount > 50) { fullGallery = false; selectedGalleryUris = emptySet(); selectedAlbum = null }
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { fullGallery = false; selectedGalleryUris = emptySet(); selectedAlbum = null }) {
                                Icon(Icons.Filled.Close, "关闭")
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(selectedAlbum ?: "全部图片", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            // 确认按钮（有选中时显示）
                            if (selectedGalleryUris.isNotEmpty()) {
                                Surface(
                                    onClick = {
                                        onAddImages(selectedGalleryUris.toList())
                                        fullGallery = false; selectedGalleryUris = emptySet(); selectedAlbum = null; expanded = false
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text("完成 (${selectedGalleryUris.size})", Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                    // 相册分类
                    if (galleryAlbums.isNotEmpty()) {
                        LazyRow(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                val isSelected = selectedAlbum == null
                                Surface(onClick = { selectedAlbum = null }, shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                                    Text("全部", Modifier.padding(horizontal = 14.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            items(galleryAlbums.size) { idx ->
                                val album = galleryAlbums[idx]
                                val isSelected = selectedAlbum == album.name
                                Surface(onClick = { selectedAlbum = album.name }, shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                                    Text("${album.name} (${album.images.size})", Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                    }
                    // 图片网格（多选）
                    val displayImages = if (selectedAlbum != null) {
                        galleryAlbums.find { it.name == selectedAlbum }?.images ?: emptyList()
                    } else galleryImages
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(displayImages.size, key = { displayImages[it].toString() }) { idx ->
                            val uri = displayImages[idx]
                            val isSelected = uri in selectedGalleryUris
                            Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(4.dp)).clickable {
                                selectedGalleryUris = if (isSelected) selectedGalleryUris - uri else selectedGalleryUris + uri
                            }) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(ctx).data(uri).size(300).build(),
                                    contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                )
                                // 选中标记
                                if (isSelected) {
                                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                                    Icon(Icons.Filled.Check, "已选", Modifier.align(Alignment.Center).size(32.dp), tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Attachment Sheet ──

/** 相册分组数据 */
private data class GalleryAlbum(val name: String, val coverUri: Uri, val images: List<Uri>)

/** 从 MediaStore 查询图片，按相册分组 */
private fun queryGalleryImages(context: android.content.Context, limit: Int = 200): Pair<List<Uri>, List<GalleryAlbum>> {
    val allImages = mutableListOf<Uri>()
    val albumMap = linkedMapOf<String, MutableList<Uri>>()
    val projection = arrayOf(
        android.provider.MediaStore.Images.Media._ID,
        android.provider.MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )
    val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_MODIFIED} DESC"
    context.contentResolver.query(
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection, null, null, sortOrder
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
        val bucketCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        var count = 0
        while (cursor.moveToNext() && count < limit) {
            val id = cursor.getLong(idCol)
            val bucket = cursor.getString(bucketCol) ?: "其他"
            val uri = android.content.ContentUris.withAppendedId(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            allImages.add(uri)
            albumMap.getOrPut(bucket) { mutableListOf() }.add(uri)
            count++
        }
    }
    val albums = albumMap.map { (name, imgs) ->
        GalleryAlbum(name = name, coverUri = imgs.first(), images = imgs)
    }.sortedByDescending { it.images.size }
    return allImages to albums
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentSheet(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    generating: Boolean,
    attached: List<Uri>,
    onClearFile: (Int) -> Unit,
    model: ModelConfig?,
    available: List<ModelConfig>,
    onSelect: (ModelConfig) -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickImages: () -> Unit,
    onPickFiles: () -> Unit,
    onSelectFromGallery: (Uri) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var galleryImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionRequested = true
        if (granted) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val (images, _) = queryGalleryImages(ctx)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { galleryImages = images }
            }
        }
    }

    LaunchedEffect(Unit) {
        val perm = if (android.os.Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES
                   else android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(ctx, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val (images, _) = queryGalleryImages(ctx)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { galleryImages = images }
            }
        } else {
            permLauncher.launch(perm)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth()) {
            // ── 输入胶囊（和底部面板连为一体）──
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            ) {
                Row(Modifier.padding(start = 14.dp, end = 4.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = onTextChange,
                        placeholder = { Text("输入消息...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        singleLine = false,
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Default),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    if (generating) {
                        IconButton(onClick = onStop, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Filled.Stop, "停止", tint = MaterialTheme.colorScheme.error)
                        }
                    } else if (text.isNotBlank() || attached.isNotEmpty()) {
                        IconButton(onClick = onSend, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Send, "发送", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── 功能按钮行 ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SheetActionButton(icon = Icons.Filled.CameraAlt, label = "拍照", onClick = onTakePhoto)
                SheetActionButton(icon = Icons.Filled.Collections, label = "图片", onClick = onPickImages)
                SheetActionButton(icon = Icons.Filled.FolderOpen, label = "文件", onClick = onPickFiles)
            }
            Spacer(Modifier.height(8.dp))

            // ── 相册缩略图网格 ──
            if (hasPermission && galleryImages.isNotEmpty()) {
                Text(
                    "最近图片",
                    Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(galleryImages.size, key = { galleryImages[it].toString() }) { idx ->
                        val uri = galleryImages[idx]
                        Box(
                            Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable {
                                onSelectFromGallery(uri)
                                onDismiss() // 选图后自动关闭
                            }
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(ctx).data(uri).size(200).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            } else if (!hasPermission && permissionRequested) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("相册权限未授权", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("可通过上方「图片」按钮选择图片", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            } else if (!hasPermission) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.height(8.dp))
                    Text("正在请求相册权限...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SheetActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) { Icon(icon, label, Modifier.size(25.dp)) }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

private fun duplicateModel(vm: ConfigViewModel, m: ModelConfig, p: com.aiaggregator.app.data.model.ApiConfig): String {
    val allModels = vm.loadModels()
    val baseName = m.displayName.ifBlank { m.modelName }
    val pattern = Regex("""^${Regex.escape(baseName)} 副本(\d+)$""")
    val maxN = allModels.mapNotNull { other ->
        pattern.find(other.displayName)?.groupValues?.get(1)?.toIntOrNull()
    }.maxOrNull() ?: 0
    val copyName = "$baseName 副本${maxN + 1}"
    val newPlatform = p.copy(id = java.util.UUID.randomUUID().toString())
    vm.savePlatform(newPlatform)
    val newModel = m.copy(id = java.util.UUID.randomUUID().toString(), platformId = newPlatform.id, displayName = copyName, isDefault = false)
    vm.saveModel(newModel)
    return copyName
}

private fun ModelConfig.displayLabel(): String = displayName.ifBlank { modelName }

private fun configFeedbackLabel(p: com.aiaggregator.app.data.model.ApiConfig?, m: ModelConfig): String {
    val platformLabel = p?.platformName?.ifBlank { p.baseUrl } ?: "未知平台"
    return "$platformLabel / ${m.displayLabel()}"
}

private data class Vendor(val id: String, val label: String, val baseUrl: String, val formatType: ApiFormatType)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(
    vm: ConfigViewModel,
    refreshKey: Int,
    onAddModel: () -> Unit,
    onEditModel: (String) -> Unit
) {
    val models = remember(refreshKey) { mutableStateOf(vm.loadModels()) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }
    val ctxToast = LocalContext.current

    fun refresh() { models.value = vm.loadModels() }

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("模型配置", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "管理平台、密钥和模型，本地加密保存，兼容 OpenAI 与 Anthropic 格式。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        if (models.value.isEmpty()) {
            ElevatedCard(
                Modifier.fillMaxWidth().clickable { onAddModel() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.AddCircle, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text("添加第一个模型", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "配置 API 地址、密钥和模型名称后即可开始使用。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            models.value.forEach { m ->
                val p = vm.getPlatform(m.platformId)
                ElevatedCard(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        m.displayName.ifBlank { m.modelName },
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (m.isDefault) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                                            Text(
                                                "默认",
                                                Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(
                                            m.category.label,
                                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        m.modelName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (p != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                p.baseUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val scope = rememberCoroutineScope()
                            var testing by remember { mutableStateOf(false) }
                            FilledTonalButton(
                                onClick = {
                                    if (testing) return@FilledTonalButton
                                    if (p == null) {
                                        Toast.makeText(ctxToast, "测试失败：缺少平台配置", Toast.LENGTH_SHORT).show()
                                        return@FilledTonalButton
                                    }
                                    testing = true
                                    scope.launch {
                                        try {
                                            val result = withContext(Dispatchers.IO) {
                                                testChatService.syncChat(
                                                    listOf(com.aiaggregator.app.business.adapter.ChatMessageItem("user", "hi")), p, m.modelName
                                                )
                                            }
                                            val feedbackLabel = configFeedbackLabel(p, m)
                                            Toast.makeText(
                                                ctxToast,
                                                if (result.error == null) "${feedbackLabel} 测试成功" else "${feedbackLabel} 测试失败：${result.error}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(ctxToast, "${configFeedbackLabel(p, m)} 测试失败：${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            testing = false
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !testing
                            ) { Text(if (testing) "测试中" else "测试", style = MaterialTheme.typography.labelSmall) }
                            FilledTonalButton(
                                onClick = {
                                    val feedbackLabel = configFeedbackLabel(p, m)
                                    when {
                                        m.category == ModelCategory.IMAGE -> Toast.makeText(ctxToast, "图片生成模型不能设为默认聊天模型：${feedbackLabel}", Toast.LENGTH_SHORT).show()
                                        m.isDefault -> Toast.makeText(ctxToast, "已是默认模型：${feedbackLabel}", Toast.LENGTH_SHORT).show()
                                        else -> {
                                            vm.setDefaultModel(m.id)
                                            refresh()
                                            Toast.makeText(ctxToast, "已将 ${feedbackLabel} 设为默认模型", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text(if (m.isDefault) "已默认" else "设默认", style = MaterialTheme.typography.labelSmall) }
                            FilledTonalButton(
                                onClick = { onEditModel(m.id) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Edit, null, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("编辑", style = MaterialTheme.typography.labelSmall)
                            }
                            FilledTonalButton(
                                onClick = {
                                    if (p == null) {
                                        Toast.makeText(ctxToast, "复制失败：缺少平台配置", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val copyName = duplicateModel(vm, m, p)
                                        refresh()
                                        Toast.makeText(ctxToast, "已复制 ${configFeedbackLabel(p, m)} 为 ${copyName}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, null, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("复制", style = MaterialTheme.typography.labelSmall)
                            }
                            FilledTonalButton(
                                onClick = { deleteConfirmId = m.id },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Delete, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(4.dp))
                                Text("删除", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (deleteConfirmId != null) {
            val modelToDelete = models.value.find { it.id == deleteConfirmId }
            val platformToDelete = modelToDelete?.let { vm.getPlatform(it.platformId) }
            val deleteLabel = modelToDelete?.let { configFeedbackLabel(platformToDelete, it) }.orEmpty()
            AlertDialog(
                onDismissRequest = { deleteConfirmId = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除「${deleteLabel}」吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(onClick = {
                        val targetId = deleteConfirmId
                        if (targetId != null) {
                            vm.deleteModel(targetId)
                            deleteConfirmId = null
                            refresh()
                            Toast.makeText(ctxToast, "已删除 ${deleteLabel}", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { deleteConfirmId = null }) { Text("取消") } },
                shape = RoundedCornerShape(20.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        ElevatedCard(
            Modifier.fillMaxWidth().clickable { onAddModel() },
            shape = RoundedCornerShape(18.dp),
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

