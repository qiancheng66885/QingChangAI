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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.window.Popup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TravelExplore
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.platform.LocalDensity
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.aiaggregator.app.R
import com.aiaggregator.app.base.ext.safeStartActivity
import com.aiaggregator.app.data.local.ImageStorageManager
import com.aiaggregator.app.data.local.SettingsStore
import com.aiaggregator.app.base.utils.TimeUtil
import com.aiaggregator.app.data.model.AppLanguage
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
import com.aiaggregator.app.ui.settings.FeatureManagementScreen
import com.aiaggregator.app.ui.settings.FeedbackScreen
import com.aiaggregator.app.ui.settings.LanguageSettingsScreen
import com.aiaggregator.app.ui.settings.PermissionManagementScreen
import com.aiaggregator.app.ui.settings.PrivacyPermissionScreen
import com.aiaggregator.app.ui.settings.SettingsScreen
import com.aiaggregator.app.ui.settings.SupportScreen
import com.aiaggregator.app.ui.settings.ThemeSettingsScreen
import com.aiaggregator.app.ui.theme.AiAggregatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Screen {
    CHAT,
    SETTINGS,
    CONFIG,
    CONFIG_EDIT,
    DATA,
    SUPPORT,
    ABOUT,
    THEME,
    LANGUAGE,
    FEATURE,
    PERMISSIONS,
    PRIVACY,
    FEEDBACK
}
private val testChatService by lazy { com.aiaggregator.app.business.chat.ChatService() }
private const val MAX_INPUT_ATTACHMENTS = 9
private val AppBg = Color.White
private val AppSurface = Color.White
private val AppPrimary = Color(0xFF0F63FF)
private val AppPrimarySoft = Color(0xFFEAF2FF)
private val AppPrimaryBorder = Color(0xFFBFD4FF)
private val AppText = Color(0xFF101B3D)
private val AppBody = Color(0xFF253454)
private val AppSubText = Color(0xFF64708F)
private val AppMuted = Color(0xFF667292)
private val AppHint = Color(0xFF7A86A3)
private val AppPlaceholder = Color(0xFF98A2B8)
private val AppBorder = Color(0xFFE1E6F0)
private val AppPanel = Color(0xFFF7F9FD)
private val AppButtonSoft = Color(0xFFF1F5FF)
private val AppIconMuted = Color(0xFF9AA5BC)

private data class AppUiColors(
    val background: Color,
    val surface: Color,
    val panel: Color,
    val soft: Color,
    val border: Color,
    val text: Color,
    val body: Color,
    val subText: Color,
    val muted: Color,
    val hint: Color,
    val placeholder: Color,
    val iconMuted: Color,
    val primary: Color,
    val primarySoft: Color,
    val primaryBorder: Color
)

@Composable
private fun appUiColors(): AppUiColors {
    val c = MaterialTheme.colorScheme
    return AppUiColors(
        background = c.background,
        surface = c.surface,
        panel = c.surfaceContainerHighest,
        soft = c.primaryContainer.copy(alpha = 0.55f),
        border = c.outlineVariant,
        text = c.onSurface,
        body = c.onSurface,
        subText = c.onSurfaceVariant,
        muted = c.onSurfaceVariant,
        hint = c.onSurfaceVariant.copy(alpha = 0.72f),
        placeholder = c.onSurfaceVariant.copy(alpha = 0.52f),
        iconMuted = c.onSurfaceVariant.copy(alpha = 0.62f),
        primary = c.primary,
        primarySoft = c.primaryContainer.copy(alpha = 0.62f),
        primaryBorder = c.primary.copy(alpha = 0.28f)
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.aiaggregator.app.data.local.InMemoryStore.initialize(this)
        setContent { AiAggregatorTheme { MainApp() } }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch(Dispatchers.IO) { com.aiaggregator.app.data.local.InMemoryStore.flushMessages() }
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
    val availableModels by chatVM.availableModelsState.collectAsState()
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
    val appSettings by remember { SettingsStore(ctx) }.settingsFlow.collectAsState(initial = null)
    val focusManager = LocalFocusManager.current
    val kbController = LocalSoftwareKeyboardController.current
    val generating = messages.lastOrNull()?.status == MessageStatus.STREAMING
    val currentLanguage = appSettings?.language ?: AppLanguage.CHINESE
    fun appText(zh: String, en: String): String {
        val resolved = when (currentLanguage) {
            AppLanguage.SYSTEM -> if (java.util.Locale.getDefault().language == "zh") AppLanguage.CHINESE else AppLanguage.ENGLISH
            else -> currentLanguage
        }
        return if (resolved == AppLanguage.ENGLISH) en else zh
    }

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
    fun addAttachments(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val known = attachedFiles.map { it.toString() }.toMutableSet()
        val merged = attachedFiles.toMutableList()
        var added = 0
        uris.forEach { uri ->
            if (merged.size < MAX_INPUT_ATTACHMENTS && known.add(uri.toString())) {
                merged.add(uri)
                added++
            }
        }
        attachedFiles = merged
        if (added < uris.size && merged.size >= MAX_INPUT_ATTACHMENTS) {
            Toast.makeText(ctx, "最多上传 $MAX_INPUT_ATTACHMENTS 个附件", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            addAttachments(listOf(tempPhotoUri!!))
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
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_INPUT_ATTACHMENTS)) { uris ->
        addAttachments(uris)
    }
    // 文件选择器
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        addAttachments(uris)
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
                onRename = { sid, title -> chatVM.renameSession(sid, title) },
                uiText = ::appText,
                onSettings = {
                    scope.launch { currentScreen = Screen.SETTINGS; drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (currentScreen == Screen.CHAT) {
                    val ui = appUiColors()
                    Surface(color = ui.surface) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .height(42.dp)
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Filled.Menu, appText("菜单", "Menu"), Modifier.size(24.dp), tint = ui.text)
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ChatTopModelPicker(
                                    model = activeModel,
                                    available = availableModels,
                                    onSelect = { chatVM.switchModel(it) }
                                )
                            }
                            IconButton(
                                onClick = { chatVM.createNewSession() },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Filled.Add, appText("新建", "New"), Modifier.size(24.dp), tint = ui.text)
                            }
                        }
                    }
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
                                    Screen.THEME -> Screen.SETTINGS
                                    Screen.LANGUAGE -> Screen.SETTINGS
                                    Screen.FEATURE -> Screen.SETTINGS
                                    Screen.PERMISSIONS -> Screen.SETTINGS
                                    Screen.PRIVACY -> Screen.SETTINGS
                                    Screen.FEEDBACK -> Screen.SETTINGS
                                    Screen.CHAT -> Screen.CHAT
                                }
                            }) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, appText("返回", "Back"), Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        },
                        title = {
                            Text(
                                when (currentScreen) {
                                    Screen.SETTINGS -> appText("我的", "Me")
                                    Screen.CONFIG -> appText("模型管理", "Models")
                                    Screen.CONFIG_EDIT -> if (editingModelId == null) appText("新增模型", "Add model") else appText("编辑模型", "Edit model")
                                    Screen.DATA -> appText("数据管理", "Data")
                                    Screen.SUPPORT -> appText("软件支持与教程", "Support")
                                    Screen.ABOUT -> appText("关于", "About")
                                    Screen.THEME -> appText("主题色彩", "Theme")
                                    Screen.LANGUAGE -> appText("语言切换", "Language")
                                    Screen.FEATURE -> appText("功能管理", "Features")
                                    Screen.PERMISSIONS -> appText("授权管理", "Authorization")
                                    Screen.PRIVACY -> appText("隐私与权限", "Privacy")
                                    Screen.FEEDBACK -> appText("功能反馈", "Feedback")
                                    else -> ""
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
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
                    Screen.CHAT -> Column(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Box(Modifier.weight(1f)) {
                            ChatView(
                                messages, ctx, activeModel, genHint = genHint,
                                uiText = ::appText,
                                onSuggestionClick = { inputText = it },
                                onRegenerate = { chatVM.regenerate() },
                                onCompleteReasoningOnly = { chatVM.completeReasoningOnly(it) },
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
                            text = inputText, onTextChange = { inputText = it },
                            uiText = ::appText,
                            onSend = { deepThink ->
                                kbController?.hide()
                                if (inputText.isNotBlank() || attachedFiles.isNotEmpty()) {
                                    chatVM.sendMessage(inputText.ifBlank { "请描述这些图片" }, attachedFiles, editImage = editImage, deepThink = deepThink)
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
                            onAddImages = { uris -> addAttachments(uris) }
                        )
                    }
                    Screen.SETTINGS -> SettingsScreen(
                        onNavConfig = { currentScreen = Screen.CONFIG },
                        onNavData = { currentScreen = Screen.DATA },
                        onNavSupport = { currentScreen = Screen.SUPPORT },
                        onNavAbout = { currentScreen = Screen.ABOUT },
                        onNavTheme = { currentScreen = Screen.THEME },
                        onNavLanguage = { currentScreen = Screen.LANGUAGE },
                        onNavFeature = { currentScreen = Screen.FEATURE },
                        onNavPermissions = { currentScreen = Screen.PERMISSIONS },
                        onNavPrivacy = { currentScreen = Screen.PRIVACY },
                        onNavFeedback = { currentScreen = Screen.FEEDBACK }
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
                        },
                        onConfigChanged = { chatVM.refreshConfig() }
                    )
                    Screen.CONFIG_EDIT -> ConfigEditScreen(
                        vm = configVM,
                        modelId = editingModelId,
                        onCancel = { currentScreen = Screen.CONFIG },
                        onSaved = {
                            configRefreshKey++
                            chatVM.refreshConfig()
                            currentScreen = Screen.CONFIG
                        }
                    )
                    Screen.DATA -> DataScreen()
                    Screen.SUPPORT -> SupportScreen()
                    Screen.ABOUT -> AboutScreen()
                    Screen.THEME -> ThemeSettingsScreen()
                    Screen.LANGUAGE -> LanguageSettingsScreen()
                    Screen.FEATURE -> FeatureManagementScreen()
                    Screen.PERMISSIONS -> PermissionManagementScreen()
                    Screen.PRIVACY -> PrivacyPermissionScreen()
                    Screen.FEEDBACK -> FeedbackScreen()
                }
            }
        }
    }
}

// ── Chat ──

@Composable
private fun ChatTopModelPicker(
    model: ModelConfig?,
    available: List<ModelConfig>,
    onSelect: (ModelConfig) -> Unit
) {
    val ui = appUiColors()
    var expanded by remember { mutableStateOf(false) }
    val label = model?.modelDisplayTitle() ?: "选择模型"

    Box(
        modifier = Modifier.width(268.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .clickable(enabled = available.isNotEmpty()) { expanded = true }
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ui.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(3.dp))
            Icon(Icons.Filled.ArrowDropDown, null, Modifier.size(20.dp), tint = ui.hint)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = 6.dp),
            modifier = Modifier
                .width(268.dp)
                .heightIn(max = 360.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = ui.surface,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, ui.border.copy(alpha = 0.75f))
        ) {
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(
                    "选择本次对话模型",
                    Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ui.muted
                )
                available.forEachIndexed { index, item ->
                    ModelPickerOption(
                        model = item,
                        selected = item.id == model?.id,
                        onClick = {
                            onSelect(item)
                            expanded = false
                        }
                    )
                    if (index != available.lastIndex) {
                        HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = ui.border.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelPickerOption(
    model: ModelConfig,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ui = appUiColors()
    val title = model.modelDisplayTitle()
    val description = model.modelShortDescription()
    val identity = model.modelIdentityLine()
    val titleColor = if (selected) ui.primary else ui.text
    val detailColor = if (selected) ui.primary.copy(alpha = 0.78f) else ui.subText

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                description,
                style = MaterialTheme.typography.labelLarge,
                color = detailColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ui.surface,
                    border = BorderStroke(1.dp, if (selected) ui.primaryBorder else ui.border)
                ) {
                    Text(
                        model.category.shortLabel(),
                        Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) ui.primary else ui.muted,
                        maxLines = 1
                    )
                }
                Text(
                    identity,
                    style = MaterialTheme.typography.labelSmall,
                    color = ui.hint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        if (selected) {
            Surface(
                Modifier.size(24.dp),
                shape = RoundedCornerShape(50),
                color = ui.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, Modifier.size(16.dp), tint = Color.White)
                }
            }
        } else {
            Box(Modifier.size(24.dp))
        }
    }
}

private fun ModelConfig.modelDisplayTitle(): String = displayName.ifBlank { modelName }.ifBlank { "未命名模型" }

private fun ModelConfig.modelIdentityLine(): String {
    val id = modelName.takeIf { it.isNotBlank() && it != modelDisplayTitle() }
    return id?.let { "API: $it" } ?: "当前模型"
}

private fun ModelConfig.modelShortDescription(): String {
    val name = "${displayName} ${modelName}".lowercase()
    return when (category) {
        ModelCategory.IMAGE -> "图片生成，适合文生图、海报和创意画面"
        ModelCategory.VIDEO -> "视频生成，适合短片、动态画面和镜头创作"
        ModelCategory.AUDIO -> "音频生成，适合语音、音乐和声音处理"
        ModelCategory.MULTIMODAL -> "可识别图片，适合看图分析和综合问答"
        ModelCategory.CHAT -> when {
            listOf("fast", "flash", "mini", "lite", "turbo", "quick", "快速", "快").any { it in name } ->
                "快速响应，适合日常问答和轻量任务"
            listOf("reason", "think", "thinking", "r1", "o1", "o3", "推理", "思考").any { it in name } ->
                "深度推理，适合复杂问题和多步分析"
            listOf("agent", "codex", "code", "tool", "computer", "工具", "代码").any { it in name } ->
                "工具与代码能力更强，适合执行复杂任务"
            listOf("max", "pro", "plus", "sonnet", "opus", "gpt-5", "gpt-4", "千问", "qwen").any { it in name } ->
                "综合能力均衡，适合工作、学习和长文本"
            else -> "综合对话，适合工作、学习和生活问题"
        }
    }
}

private fun ModelCategory.shortLabel(): String = when (this) {
    ModelCategory.CHAT -> "语言模型"
    ModelCategory.IMAGE -> "图片模型"
    ModelCategory.VIDEO -> "视频模型"
    ModelCategory.AUDIO -> "音频模型"
    ModelCategory.MULTIMODAL -> "多模态"
}

@Composable
private fun ChatView(messages: List<Message>, ctx: Context, activeModel: ModelConfig?, genHint: String = "", uiText: (String, String) -> String = { zh, _ -> zh }, onSuggestionClick: (String) -> Unit, onRegenerate: () -> Unit = {}, onCompleteReasoningOnly: (String) -> Unit = {}, onStop: () -> Unit = {}, onUserScroll: () -> Unit = {}) {
    val ui = appUiColors()
    if (messages.isEmpty()) {
        val suggestionPool = remember(uiText("zh", "en")) {
            listOf(
                uiText("用简单的语言解释量子计算", "Explain quantum computing in simple words"),
                uiText("写一首关于夏天的诗", "Write a poem about summer"),
                uiText("帮我规划三天北京旅行", "Plan a three-day trip to Beijing"),
                uiText("推荐几本好看的科幻小说", "Recommend some good sci-fi novels"),
                uiText("怎么做番茄炒蛋", "How do I make tomato scrambled eggs?"),
                uiText("Python 和 Java 哪个更适合初学者", "Is Python or Java better for beginners?"),
                uiText("用一句话解释黑洞", "Explain black holes in one sentence"),
                uiText("帮我写一封辞职邮件", "Help me write a resignation email"),
                uiText("最近有什么好看的电影", "What are some good movies to watch?"),
                uiText("如何提高英语口语", "How can I improve spoken English?"),
                uiText("解释什么是机器学习", "Explain what machine learning is"),
                uiText("讲一个睡前故事", "Tell me a bedtime story"),
                uiText("推荐适合新手的健身计划", "Recommend a beginner fitness plan"),
                uiText("30分钟能做哪些快手菜", "What quick meals can I cook in 30 minutes?")
            )
        }
        val shown = remember { suggestionPool.shuffled().take(4) }

        Column(
            Modifier
                .fillMaxSize()
                .background(ui.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(86.dp))
            Image(
                painter = painterResource(R.drawable.app_logo_transparent),
                contentDescription = uiText("清畅AI", "QingChang AI"),
                modifier = Modifier.size(78.dp)
            )
            Spacer(Modifier.height(34.dp))
            Text(
                uiText("嗨，今天想聊点什么？", "Hi, what would you like to talk about today?"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = ui.text
            )
            Spacer(Modifier.height(10.dp))
            Text(
                uiText("多平台 AI 模型，一站式对话", "Multi-platform AI models in one chat"),
                style = MaterialTheme.typography.bodyLarge,
                color = ui.subText
            )
            Spacer(Modifier.height(28.dp))
            shown.forEach { s ->
                Surface(
                    Modifier
                        .padding(vertical = 6.dp)
                        .clickable { onSuggestionClick(s) },
                    shape = RoundedCornerShape(20.dp),
                    color = ui.surface,
                    border = BorderStroke(1.dp, ui.border)
                ) {
                    Text(
                        s,
                        Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ui.body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        val lastMsg = messages.lastOrNull()
        val isStreaming = lastMsg?.status == com.aiaggregator.app.data.model.MessageStatus.STREAMING
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current
        val nearBottomThresholdPx = with(density) { 32.dp.roundToPx() }
        val readingTopThresholdPx = with(density) { 8.dp.roundToPx() }
        val readingOverflowThresholdPx = with(density) { 48.dp.roundToPx() }

        // ─── 跟随控制 ───
        var userScrolledUp by remember { mutableStateOf(false) }
        var continuousFollow by remember { mutableStateOf(false) }
        var initialFollowStopped by remember { mutableStateOf(false) }
        var programmaticScroll by remember { mutableStateOf(false) }
        val activeStreamingId = if (isStreaming) lastMsg?.id else null
        val streamingContentLength = if (activeStreamingId != null) lastMsg?.content?.length ?: 0 else 0
        val latestDoneAssistantIndex = remember(messages) {
            messages.indexOfLast { it.role == MessageRole.ASSISTANT && it.status != MessageStatus.STREAMING }
        }
        val isNearBottom by remember(listState, messages.size) {
            derivedStateOf {
                if (messages.isEmpty()) {
                    true
                } else if (!listState.canScrollForward) {
                    true
                } else {
                    val layoutInfo = listState.layoutInfo
                    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull { it.index == messages.lastIndex }
                    val distanceToBottom = lastItem?.let {
                        it.offset + it.size - layoutInfo.viewportEndOffset
                    } ?: Int.MAX_VALUE
                    distanceToBottom <= nearBottomThresholdPx
                }
            }
        }

        suspend fun scrollToLatestMessage() {
            if (messages.isEmpty()) return
            programmaticScroll = true
            try {
                listState.scrollToItem(messages.lastIndex, Int.MAX_VALUE)
            } finally {
                programmaticScroll = false
            }
        }

        LaunchedEffect(listState, isStreaming) {
            snapshotFlow { listState.isScrollInProgress to isNearBottom }
                .collect { (scrolling, nearBottom) ->
                    if (!programmaticScroll && scrolling && !nearBottom) {
                        onUserScroll()
                        userScrolledUp = true
                        continuousFollow = false
                    } else if (!programmaticScroll && !scrolling && nearBottom) {
                        userScrolledUp = false
                        continuousFollow = isStreaming
                    }
                }
        }

        var canScrollDebounced by remember { mutableStateOf(false) }

        LaunchedEffect(isNearBottom) {
            if (!isNearBottom) {
                delay(250)
                canScrollDebounced = true
            } else {
                canScrollDebounced = false
            }
        }

        val showButton = canScrollDebounced && !isNearBottom

        LaunchedEffect(activeStreamingId) {
            if (activeStreamingId != null) {
                userScrolledUp = false
                continuousFollow = false
                initialFollowStopped = false
                canScrollDebounced = false
                scrollToLatestMessage()
            } else {
                continuousFollow = false
                if (isNearBottom) {
                    userScrolledUp = false
                    initialFollowStopped = false
                }
            }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty() && activeStreamingId == null && !userScrolledUp) {
                canScrollDebounced = false
                scrollToLatestMessage()
            }
        }

        LaunchedEffect(activeStreamingId, streamingContentLength, userScrolledUp, continuousFollow, initialFollowStopped) {
            if (activeStreamingId == null || messages.isEmpty()) return@LaunchedEffect
            if (userScrolledUp && !continuousFollow) return@LaunchedEffect

            if (continuousFollow) {
                scrollToLatestMessage()
                return@LaunchedEffect
            }
            if (initialFollowStopped) return@LaunchedEffect

            val layoutInfo = listState.layoutInfo
            val currentInfo = layoutInfo.visibleItemsInfo.find { it.index == messages.lastIndex }
            val reachedReadingStop = currentInfo != null &&
                currentInfo.offset <= layoutInfo.viewportStartOffset + readingTopThresholdPx &&
                currentInfo.offset + currentInfo.size >= layoutInfo.viewportEndOffset + readingOverflowThresholdPx

            if (reachedReadingStop) {
                initialFollowStopped = true
                userScrolledUp = true
                canScrollDebounced = true
            } else {
                scrollToLatestMessage()
            }
        }

        Box(Modifier.fillMaxSize().background(ui.background)) {
            LazyColumn(
                Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp)
            ) {
                items(
                    count = messages.size,
                    key = { messages[it].id },
                    contentType = { messages[it].contentType.name }
                ) { idx ->
                    val msg = messages[idx]
                    val prev = if (idx > 0) messages[idx - 1].role else null
                    val next = if (idx < messages.lastIndex) messages[idx + 1].role else null
                    val canRegenerate = msg.role == MessageRole.ASSISTANT &&
                        msg.status != MessageStatus.STREAMING &&
                        idx == latestDoneAssistantIndex
                    ChatBubble(msg, prev, next, ctx, onRegenerate, onCompleteReasoningOnly, onStop, genHint, canRegenerate)
                }
            }
            // "回到底部"悬浮按钮
            AnimatedVisibility(
                visible = showButton,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        continuousFollow = isStreaming
                        initialFollowStopped = true
                        userScrolledUp = false
                        canScrollDebounced = false
                        coroutineScope.launch {
                            scrollToLatestMessage()
                        }
                    },
                    containerColor = ui.surface,
                    contentColor = ui.body,
                    shape = RoundedCornerShape(50),
                    elevation = FloatingActionButtonDefaults.elevation(2.dp, 2.dp)
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, uiText("回到底部", "Back to bottom"))
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    msg: Message,
    prev: MessageRole?,
    next: MessageRole?,
    ctx: Context,
    onRegenerate: () -> Unit = {},
    onCompleteReasoningOnly: (String) -> Unit = {},
    onStop: () -> Unit = {},
    genHint: String = "",
    canRegenerate: Boolean = false
) {
    val isUser = msg.role == MessageRole.USER
    val streaming = msg.status == MessageStatus.STREAMING
    val first = prev != msg.role
    val last = next != msg.role
    val images = msg.allImageUrls
    val reasoningText = msg.reasoningContent?.takeIf { it.isNotBlank() }
    val deepThinkingRequested = msg.metadata?.contains("deepThink=true") == true
    val streamIdle = msg.metadata?.contains("streamIdle=true") == true
    val reasoningOnlyFallback = !isUser && reasoningText != null && msg.status != MessageStatus.STREAMING &&
        (msg.metadata?.contains("reasoningOnly=true") == true ||
            msg.content.startsWith("模型返回了思考过程") ||
            msg.content.startsWith("已收到模型的思考过程"))
    val isAiImageMsg = !isUser && msg.contentType == com.aiaggregator.app.data.model.ContentType.IMAGE && (images.isNotEmpty() || streaming)
    val textActionsEnabled = msg.content.isNotBlank() && !streaming
    var showTextActions by remember(msg.id) { mutableStateOf(false) }
    var showTextSelection by remember(msg.id) { mutableStateOf(false) }
    val showMessageTextActions = {
        if (textActionsEnabled) showTextActions = true
    }
    val longPressTextActions = Modifier.pointerInput(msg.id, msg.content, streaming) {
        detectTapGestures(onLongPress = {
            showMessageTextActions()
        })
    }

    if (showTextActions) {
        MessageTextActionSheet(
            text = msg.content,
            canRegenerate = !isUser && canRegenerate,
            onCopy = {
                copyText(ctx, msg.content)
                showTextActions = false
            },
            onSelectText = {
                showTextActions = false
                showTextSelection = true
            },
            onShare = {
                shareContent(ctx, msg.content, false)
                showTextActions = false
            },
            onRegenerate = {
                showTextActions = false
                onRegenerate()
            },
            onMore = {
                Toast.makeText(ctx, "更多功能开发中", Toast.LENGTH_SHORT).show()
                showTextActions = false
            },
            onDismiss = { showTextActions = false }
        )
    }
    if (showTextSelection) {
        MessageTextSelectionDialog(
            text = msg.content,
            onCopy = { copyText(ctx, msg.content) },
            onDismiss = { showTextSelection = false }
        )
    }

    // ── AI 图片消息：图片独立，不在气泡里 ──
    if (isAiImageMsg) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            // 生成中：显示加载动画
            if (streaming) {
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
                        canRegenerate = canRegenerate,
                        onShare = { shareContent(ctx, images[0], true) },
                        onSave = { saveImageToDevice(ctx, images[0]) },
                        onCopy = { copyImageReference(ctx, images[0]) })
                } else {
                    ImageGallery(images = images,
                        onImageClick = { idx -> fullIndex = idx; showFull = true },
                        onRegenerate = onRegenerate,
                        canRegenerate = canRegenerate,
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
                    modifier = longPressTextActions,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(msg.content, Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        return
    }

    // ── AI 文字消息：无气泡，文字直接展示，几乎占满宽度 ──
    if (!isUser) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(longPressTextActions)
                .padding(horizontal = 16.dp, vertical = if (last) 8.dp else 3.dp)
        ) {
            // 打字动画
            if (streaming && msg.content.isEmpty()) {
                TypingDots(if (deepThinkingRequested) "正在深度思考，等待首个回复..." else "正在连接模型，等待首个回复...")
            }
            if (streaming && streamIdle) {
                StreamIdleHint(deepThinking = deepThinkingRequested)
                Spacer(Modifier.height(8.dp))
            }
            if (reasoningText != null) {
                ReasoningBlock(
                    text = reasoningText,
                    streaming = streaming,
                    hasAnswer = msg.content.isNotBlank(),
                    startedAt = msg.timestamp
                )
                Spacer(Modifier.height(8.dp))
            }
            // 文字内容（无容器，直接在背景上，按块渲染）
            if (reasoningOnlyFallback) {
                ReasoningOnlyFallbackCard(
                    onComplete = { onCompleteReasoningOnly(msg.id) },
                    onRegenerate = onRegenerate,
                    canRegenerate = canRegenerate
                )
            } else if (msg.content.isBlank() && !streaming) {
                Text(
                    "⚠️ 未收到可显示内容，请点击重新生成或切换模型后重试。",
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                )
            }
            if (msg.content.isNotBlank() && !reasoningOnlyFallback) {
                val hColor = MaterialTheme.colorScheme.primary
                val qColor = MaterialTheme.colorScheme.onSurfaceVariant
                val cColor = MaterialTheme.colorScheme.secondary
                val bodyFontSize = MaterialTheme.typography.bodyMedium.fontSize
                if (streaming) {
                    StreamingTextView(msg.content, bodyFontSize)
                } else {
                    val blocks = remember(msg.id, msg.content) { parseMarkdownBlocks(msg.content) }
                    blocks.forEach { block ->
                        when (block) {
                            is MarkdownBlock.CodeBlock -> CodeBlockView(block.language, block.code)
                            is MarkdownBlock.TableBlock -> TableView(block.headers, block.rows)
                            is MarkdownBlock.ImageBlock -> MarkdownImageView(block.url, block.alt, ctx)
                            is MarkdownBlock.AlertBlock -> AlertBlockView(block.type, block.lines, hColor, qColor, cColor)
                            is MarkdownBlock.DetailsBlock -> DetailsBlockView(block.title, block.lines, hColor, qColor, cColor)
                            is MarkdownBlock.ListBlock -> ListBlockView(block.items, hColor, qColor, cColor)
                            is MarkdownBlock.TextBlock -> {
                                val blockText = block.lines.joinToString("\n")
                                val annotated = remember(blockText, hColor, qColor, cColor, bodyFontSize) {
                                    parseMarkdown(blockText, hColor, qColor, cColor, bodyFontSize)
                                }
                                val textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = bodyFontSize * 1.55
                                )
                                var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                                Text(
                                    annotated,
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .pointerInput(annotated, textActionsEnabled) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    showMessageTextActions()
                                                },
                                                onTap = { offset ->
                                                    textLayoutResult?.let { layout ->
                                                        val pos = layout.getOffsetForPosition(offset)
                                                        annotated.getStringAnnotations("URL", pos, pos).firstOrNull()?.let {
                                                            openUrlSafely(ctx, it.item)
                                                        }
                                                    }
                                                }
                                            )
                                        },
                                    style = textStyle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    onTextLayout = { textLayoutResult = it }
                                )
                            }
                        }
                    }
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
                if (!reasoningOnlyFallback) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (canRegenerate) {
                            ActionText(Icons.Filled.Refresh, "重新生成", onRegenerate)
                        }
                        ActionText(Icons.Filled.Share, "分享") { shareContent(ctx, msg.content, false) }
                        ActionText(Icons.Filled.ContentCopy, "复制") { copyText(ctx, msg.content) }
                    }
                }
            }
        }
        return
    }

    // ── 用户文字消息：极淡圆角气泡，右对齐 ──
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = if (last) 6.dp else 2.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            Modifier.widthIn(max = 280.dp).then(longPressTextActions),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ) {
            Text(
                msg.content,
                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageTextActionSheet(
    text: String,
    canRegenerate: Boolean,
    onCopy: () -> Unit,
    onSelectText: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit,
    onMore: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 26.dp)
        ) {
            Text(
                text.take(80).ifBlank { "消息操作" },
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
            MessageActionRow(Icons.Filled.ContentCopy, "复制", onCopy)
            MessageActionRow(Icons.Filled.EditNote, "选择文本", onSelectText)
            if (canRegenerate) {
                MessageActionRow(Icons.Filled.Refresh, "重新生成", onRegenerate)
            }
            MessageActionRow(Icons.Filled.Share, "分享", onShare)
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = AppBorder)
            MessageActionRow(Icons.Filled.MoreHoriz, "更多", onMore)
        }
    }
}

@Composable
private fun MessageActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, Modifier.size(24.dp), tint = AppText)
        Spacer(Modifier.width(18.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = AppText)
    }
}

@Composable
private fun MessageTextSelectionDialog(
    text: String,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text("选择文本") },
        text = {
            SelectionContainer {
                Text(
                    text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppText
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onCopy()
                onDismiss()
            }) { Text("复制全部") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        }
    )
}

@Composable
private fun StreamingTextView(text: String, bodyFontSize: TextUnit) {
    val displayText = remember(text) { "$text▌" }
    Text(
        displayText,
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = bodyFontSize * 1.55),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ReasoningOnlyFallbackCard(
    onComplete: () -> Unit,
    onRegenerate: () -> Unit,
    canRegenerate: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFF),
        border = BorderStroke(1.dp, Color(0xFFE1E8F6))
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEAF1FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Psychology, "已收到思考", Modifier.size(15.dp), tint = AppPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "已收到思考，正文未返回",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppBody,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "这通常是中转站只透传了 reasoning/thinking，或回复在正文前被截断。可以让模型基于上面的思考摘要补写最终答案。",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = AppMuted
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionText(Icons.Filled.AutoAwesome, "补出答案", onComplete)
                if (canRegenerate) {
                    ActionText(Icons.Filled.Refresh, "重新生成", onRegenerate)
                }
            }
        }
    }
}

@Composable
private fun StreamIdleHint(deepThinking: Boolean) {
    val title = if (deepThinking) "思考流暂时停顿" else "回复流暂时停顿"
    val body = if (deepThinking) {
        "中转站已经超过一小会儿没有继续返回思考片段。可能是模型正在长时间推理，也可能是上游连接卡住；可以继续等，或停止后重新生成。"
    } else {
        "中转站已经超过一小会儿没有继续返回内容。可能是上游正在排队，也可能是连接卡住；可以继续等，或停止后重新生成。"
    }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFF),
        border = BorderStroke(1.dp, Color(0xFFE1E8F6))
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.MoreHoriz,
                title,
                Modifier.size(18.dp).padding(top = 1.dp),
                tint = AppPrimary
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppBody,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = AppMuted
                )
            }
        }
    }
}

@Composable
private fun ReasoningBlock(text: String, streaming: Boolean, hasAnswer: Boolean, startedAt: Long) {
    var expanded by remember(startedAt) { mutableStateOf(streaming) }
    var autoCollapsed by remember(startedAt) { mutableStateOf(false) }
    val thinkingComplete = hasAnswer || !streaming
    val normalizedText = remember(text) { text.trim() }
    val liveText = remember(normalizedText) {
        if (normalizedText.length > 1400) "…\n" + normalizedText.takeLast(1400).trim() else normalizedText
    }
    val segments = remember(normalizedText, thinkingComplete, expanded) {
        if (thinkingComplete && expanded) reasoningSegments(normalizedText) else emptyList()
    }
    var now by remember(startedAt, streaming) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt, streaming, hasAnswer) {
        when {
            thinkingComplete && !autoCollapsed -> {
                expanded = false
                autoCollapsed = true
            }
            streaming && !autoCollapsed -> expanded = true
        }
    }
    LaunchedEffect(startedAt, streaming, thinkingComplete) {
        while (streaming && !thinkingComplete) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsedSec = ((now - startedAt).coerceAtLeast(0L) / 1000L).toInt()
    val elapsedText = when {
        elapsedSec <= 0 -> "刚刚"
        elapsedSec < 60 -> "${elapsedSec} 秒"
        else -> "${elapsedSec / 60} 分 ${elapsedSec % 60} 秒"
    }
    val statusText = if (thinkingComplete) "已完成思考" else "正在深度思考"
    val durationText = if (thinkingComplete) "用时 $elapsedText" else "已用 $elapsedText"
    val preview = when {
        thinkingComplete -> "最终答案已在下方输出，点开可查看模型公开返回的思考过程"
        streaming -> "正在接收模型公开返回的思考片段，最终答案会在下方分开显示"
        normalizedText.isBlank() -> "模型返回了思考摘要"
        else -> "点击查看模型公开返回的思考过程，最终答案在下方"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 2.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Psychology, "深度思考", Modifier.size(18.dp), tint = AppMuted)
            Spacer(Modifier.width(7.dp))
            Text(
                statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = AppMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "($durationText)",
                style = MaterialTheme.typography.bodyMedium,
                color = AppMuted
            )
            if (!thinkingComplete) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = AppPrimary,
                    trackColor = Color.Transparent
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.ArrowDropDown,
                if (expanded) "收起" else "展开",
                Modifier
                    .size(22.dp)
                    .graphicsLayer { rotationZ = if (expanded) 180f else 0f },
                tint = AppMuted
            )
        }
        if (!expanded) {
            Text(
                preview,
                Modifier.padding(start = 28.dp, end = 8.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = AppHint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp, end = 6.dp, bottom = 2.dp)
            ) {
                if (!thinkingComplete) {
                    ReasoningStep(
                        text = liveText.ifBlank { "正在等待模型继续返回思考内容…" },
                        isLast = true
                    )
                    if (normalizedText.length > liveText.length) {
                        Text(
                            "为保证流式输出顺滑，正在思考时只展示最近片段；完成后可展开查看完整过程。",
                            Modifier.padding(start = 26.dp, top = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppHint
                        )
                    }
                } else if (segments.isEmpty()) {
                    ReasoningStep(
                        text = normalizedText.ifBlank { "模型返回了思考摘要，但内容为空。" },
                        isLast = true
                    )
                } else {
                    segments.forEachIndexed { index, segment ->
                        ReasoningStep(
                            text = segment,
                            isLast = index == segments.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasoningStep(text: String, isLast: Boolean, muted: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Column(
            Modifier.width(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .padding(top = 7.dp)
                    .size(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (muted) AppHint.copy(alpha = 0.55f) else AppMuted.copy(alpha = 0.8f))
            )
            if (!isLast) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(AppBorder)
                )
            }
        }
        Text(
            text,
            Modifier
                .weight(1f)
                .padding(start = 6.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 23.sp),
            color = if (muted) AppHint else AppMuted
        )
    }
}

private fun reasoningSegments(text: String): List<String> {
    val cleaned = text
        .replace("**", "")
        .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
        .lineSequence()
        .map { it.trim() }
        .filterNot { isReasoningHeadingOnly(it) }
        .joinToString("\n")
        .trim()
    if (cleaned.isBlank()) return emptyList()

    val lineSegments = cleaned
        .lineSequence()
        .map { line ->
            line.trim()
                .removePrefix("-")
                .removePrefix("*")
                .replace(Regex("^\\d+[.)、]\\s*"), "")
                .trim()
        }
        .filter { it.isNotBlank() }
        .toList()

    val paragraphs = cleaned
        .split(Regex("\\n\\s*\\n+"))
        .map { it.replace(Regex("\\s+"), " ").trim() }
        .filter { it.isNotBlank() }

    val source = when {
        lineSegments.size >= 2 -> lineSegments
        paragraphs.size >= 2 -> paragraphs
        else -> splitReasoningSentences(cleaned)
    }

    return source
        .flatMap { splitReasoningSentences(it) }
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

private fun splitReasoningSentences(text: String): List<String> {
    val normalized = text.replace(Regex("\\s+"), " ").trim()
    if (normalized.length <= 120) return listOf(normalized)

    val sentences = Regex("[^。！？.!?；;]+[。！？.!?；;]?")
        .findAll(normalized)
        .map { it.value.trim() }
        .filter { it.isNotBlank() }
        .toList()
    if (sentences.isEmpty()) return listOf(normalized)

    val packed = mutableListOf<String>()
    val current = StringBuilder()
    sentences.forEach { sentence ->
        if (current.isNotEmpty() && current.length + sentence.length > 120) {
            packed += current.toString().trim()
            current.clear()
        }
        if (current.isNotEmpty()) current.append(' ')
        current.append(sentence)
    }
    if (current.isNotEmpty()) packed += current.toString().trim()
    return packed
}

private fun isReasoningHeadingOnly(line: String): Boolean {
    val normalized = line
        .trim()
        .trim('#', '*', '-', ' ', ':', '：')
        .lowercase()
    return normalized in setOf(
        "可读推理摘要",
        "推理摘要",
        "思考摘要",
        "思考过程",
        "reasoning summary",
        "readable reasoning summary",
        "thinking process",
        "chain of thought"
    )
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
            Color(0xFFEAF1FF),
            Color.White.copy(alpha = 0.95f),
            Color(0xFFE9EEF8)
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
    val displayHint = hint?.takeIf { it.isNotBlank() } ?: when {
        elapsedSec < 15 -> "正在提交图片生成请求..."
        elapsedSec < 45 -> "正在等待平台返回结果，不同模型速度会有差异"
        elapsedSec < 90 -> "中转站、官转或逆向接口的生成速度可能不同"
        elapsedSec < 130 -> "图片越复杂、参考图越多，等待时间通常越长"
        elapsedSec < 210 -> "图片生成用时 2 分钟左右甚至更久属于正常现象"
        elapsedSec < 300 -> "仍在等待上游返回，请尽量保持 App 在前台和网络稳定"
        else -> "已等待较久，可能是平台排队或连接不稳定；可以继续等，也可以停止后重试"
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
        Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(22.dp),
        color = AppSurface,
        border = BorderStroke(1.dp, AppBorder),
        shadowElevation = 3.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(AppPrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, "正在生成图片", Modifier.size(18.dp), tint = AppPrimary)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "正在生成图片",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "已用 $elapsedText",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppMuted
                    )
                }
                if (onStop != null) {
                    Surface(
                        modifier = Modifier.clickable { onStop() },
                        shape = RoundedCornerShape(50),
                        color = AppPanel,
                        border = BorderStroke(1.dp, AppBorder)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Stop, null, Modifier.size(13.dp), tint = AppMuted)
                            Spacer(Modifier.width(4.dp))
                            Text("停止", style = MaterialTheme.typography.labelSmall, color = AppMuted)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 320.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(brush)
            )
            {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.32f))
                            )
                        )
                )
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            null,
                            Modifier.padding(14.dp).size(30.dp),
                            tint = AppPrimary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        displayHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppBody,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                color = AppPrimary.copy(alpha = 0.7f),
                trackColor = AppPrimarySoft,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "生成期间请耐心等待，尽量不要退出或频繁切屏；部分系统、网络切换或中转站连接可能影响结果返回。",
                style = MaterialTheme.typography.labelSmall,
                color = hintColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Attachment thumbnail for input preview ──

@Composable
private fun AttachmentThumb(uri: Uri, onRemove: () -> Unit) {
    val ctx = LocalContext.current
    Box(Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(ctx).data(uri).size(96).build(),
            contentDescription = "预览",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
        )
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

// ── Reliable local/remote thumbnail loader ──

@Composable
private fun ThumbImage(url: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    AsyncImage(
        model = coil.request.ImageRequest.Builder(ctx).data(android.net.Uri.parse(url)).size(240).build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
    )
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

@Composable
private fun AdaptiveGeneratedImage(
    url: String,
    modifier: Modifier = Modifier,
    fallbackRatio: Float = 1f,
    onClick: () -> Unit
) {
    var imageRatio by remember(url) { mutableStateOf<Float?>(null) }
    val displayRatio = (imageRatio ?: fallbackRatio).coerceIn(0.46f, 2.35f)

    Surface(
        modifier = modifier
            .aspectRatio(displayRatio)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = AppPanel,
        border = BorderStroke(1.dp, AppBorder)
    ) {
        AsyncImage(
            model = android.net.Uri.parse(url),
            contentDescription = "生成的图片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            placeholder = ColorPainter(AppPanel),
            error = ColorPainter(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
            onSuccess = { state ->
                val drawable = state.result.drawable
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                if (width > 0 && height > 0) {
                    imageRatio = width.toFloat() / height.toFloat()
                }
            }
        )
    }
}

/** 单张 AI 图片：无容器，图片填满，下方独立操作行 */
@Composable
private fun AiImageCard(
    url: String, modifier: Modifier = Modifier,
    onClick: () -> Unit, onRegenerate: () -> Unit,
    canRegenerate: Boolean,
    onShare: () -> Unit, onSave: () -> Unit, onCopy: () -> Unit
) {
    Column(modifier) {
        AdaptiveGeneratedImage(
            url = url,
            modifier = Modifier.fillMaxWidth(),
            fallbackRatio = 1f,
            onClick = onClick
        )
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (canRegenerate) {
                ActionText(Icons.Filled.Refresh, "重新生成", onRegenerate)
            }
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
    canRegenerate: Boolean,
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
                Column(Modifier.width(248.dp)) {
                    AdaptiveGeneratedImage(
                        url = url,
                        modifier = Modifier.fillMaxWidth(),
                        fallbackRatio = 1f,
                        onClick = { onImageClick(idx) }
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (canRegenerate) {
                            ActionText(Icons.Filled.Refresh, "重新生成", onRegenerate)
                        }
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.checked == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    parseMarkdown(item.text, hColor, qColor, cColor, MaterialTheme.typography.bodyMedium.fontSize),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.55),
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
private fun InputFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color = AppPrimary,
    iconContainerColor: Color = AppPrimarySoft,
    onClick: () -> Unit
) {
    val ui = appUiColors()
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = ui.surface,
        border = BorderStroke(1.dp, ui.border),
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.padding(start = 7.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    Modifier.size(15.dp),
                    tint = iconTint
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = ui.body,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InputInlineChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ui = appUiColors()
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) ui.primarySoft else ui.surface,
        border = BorderStroke(1.dp, if (selected) ui.primaryBorder else ui.border)
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(14.dp), tint = if (selected) ui.primary else ui.muted)
            Spacer(Modifier.width(3.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) ui.primary else ui.body,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InputArea(
    text: String,
    onTextChange: (String) -> Unit,
    uiText: (String, String) -> String = { zh, _ -> zh },
    onSend: (Boolean) -> Unit,
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
    val ui = appUiColors()
    val canSend = text.isNotBlank() || attached.isNotEmpty()
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
    val kbCtrl = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }
    var fullGallery by remember { mutableStateOf(false) }
    var selectedGalleryUris by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var galleryImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var galleryAlbums by remember { mutableStateOf<List<GalleryAlbum>>(emptyList()) }
    var galleryLoading by remember { mutableStateOf(false) }
    var galleryHasMore by remember { mutableStateOf(true) }
    var galleryOffset by remember { mutableStateOf(0) }
    var selectedAlbum by remember { mutableStateOf<String?>(null) }
    var inputFocused by remember { mutableStateOf(false) }
    var deepThinkEnabled by remember { mutableStateOf(false) }
    var webSearchEnabled by remember { mutableStateOf(false) }
    var lastPanelToggleAt by remember { mutableStateOf(0L) }
    val galleryGridState = rememberLazyGridState()
    val galleryPageSize = 60

    suspend fun loadGalleryPage(reset: Boolean = false) {
        if (galleryLoading || (!reset && !galleryHasMore)) return
        val loadOffset = if (reset) 0 else galleryOffset
        galleryLoading = true
        try {
            val (images, albums) = withContext(Dispatchers.IO) {
                queryGalleryImages(ctx, limit = galleryPageSize, offset = loadOffset)
            }
            val existing = if (reset) emptySet() else galleryImages.map { it.toString() }.toSet()
            val newImages = images.filter { it.toString() !in existing }
            galleryImages = if (reset) newImages else galleryImages + newImages
            galleryAlbums = if (reset) albums else mergeGalleryAlbums(galleryAlbums, albums)
            galleryOffset = loadOffset + images.size
            galleryHasMore = images.size >= galleryPageSize && (reset || newImages.isNotEmpty())
        } finally {
            galleryLoading = false
        }
    }

    LaunchedEffect(fullGallery) {
        if (fullGallery && galleryImages.isEmpty()) {
            selectedAlbum = null
            selectedGalleryUris = emptySet()
            galleryHasMore = true
            galleryOffset = 0
            loadGalleryPage(reset = true)
        }
    }

    LaunchedEffect(fullGallery, selectedAlbum, galleryLoading, galleryHasMore) {
        if (!fullGallery) return@LaunchedEffect
        snapshotFlow {
            val lastVisible = galleryGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = if (selectedAlbum != null) {
                galleryAlbums.find { it.name == selectedAlbum }?.images?.size ?: 0
            } else {
                galleryImages.size
            }
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (!galleryLoading && galleryHasMore && total > 0 && lastVisible >= total - 12) {
                loadGalleryPage(reset = false)
            }
        }
    }

    AnimatedVisibility(visible = !fullGallery) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 10.dp)
        ) {
        // 生成进度条
        if (generating) {
            LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp), color = MaterialTheme.colorScheme.primary, trackColor = Color.Transparent)
        }

        LazyRow(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            item {
                InputFeatureChip(
                    icon = Icons.Filled.AutoAwesome,
                    label = uiText("AI生图", "AI image"),
                    onClick = { Toast.makeText(ctx, uiText("AI生图功能开发中", "AI image feature is coming soon"), Toast.LENGTH_SHORT).show() }
                )
            }
            item {
                InputFeatureChip(
                    icon = Icons.Filled.School,
                    label = uiText("拍题答疑", "Homework"),
                    onClick = { Toast.makeText(ctx, uiText("拍题答疑功能开发中", "Homework feature is coming soon"), Toast.LENGTH_SHORT).show() }
                )
            }
            item {
                InputFeatureChip(
                    icon = Icons.Filled.EditNote,
                    label = uiText("帮我写作", "Writing"),
                    onClick = { Toast.makeText(ctx, uiText("帮我写作功能开发中", "Writing feature is coming soon"), Toast.LENGTH_SHORT).show() }
                )
            }
        }

        // ── 附件预览 ──
        AnimatedVisibility(visible = attached.isNotEmpty()) {
            LazyRow(
                Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val previewCount = minOf(attached.size, MAX_INPUT_ATTACHMENTS)
                items(previewCount, key = { attached[it] }) { idx ->
                    AttachmentThumb(uri = attached[idx], onRemove = { onClearFile(idx) })
                }
            }
        }

        // ── 输入胶囊（独立悬浮）──
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ui.surface,
            border = BorderStroke(1.dp, ui.border),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ) {
            Column(Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 34.dp, max = 104.dp)
                            .onFocusChanged {
                                inputFocused = it.isFocused
                                if (it.isFocused) expanded = false
                            },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = ui.text,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(ui.primary),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Default),
                        maxLines = 4,
                        decorationBox = { innerTextField ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                if (text.isEmpty()) {
                                    Text(
                                        uiText("发消息...", "Message..."),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ui.iconMuted
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InputInlineChip(
                            icon = Icons.Filled.Psychology,
                            label = uiText("思考", "Think"),
                            selected = deepThinkEnabled,
                            onClick = { deepThinkEnabled = !deepThinkEnabled }
                        )
                        Spacer(Modifier.width(8.dp))
                        InputInlineChip(
                            icon = Icons.Filled.TravelExplore,
                            label = uiText("联网", "Web"),
                            selected = webSearchEnabled,
                            onClick = { webSearchEnabled = !webSearchEnabled }
                        )
                        Spacer(Modifier.weight(1f))
                        AnimatedVisibility(visible = generating) {
                            IconButton(onClick = onStop, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.StopCircle, "停止", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        AnimatedVisibility(visible = !generating) {
                            Row {
                                IconButton(
                                    onClick = {
                                        val now = System.currentTimeMillis()
                                        if (now - lastPanelToggleAt > 250) {
                                            lastPanelToggleAt = now
                                            focusManager.clearFocus(force = true)
                                            kbCtrl?.hide()
                                            expanded = !expanded
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(if (expanded) Icons.Filled.Close else Icons.Filled.AddCircleOutline, if (expanded) uiText("收起", "Collapse") else uiText("展开", "Expand"), Modifier.size(22.dp), tint = ui.body)
                                }
                                if (canSend) {
                                    IconButton(onClick = { onSend(deepThinkEnabled) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
            }
        }

        // ── 展开内容（无容器，键盘弹出时自动收起）──
        AnimatedVisibility(visible = expanded && !fullGallery && !inputFocused) {
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(8.dp))
                // 功能按钮（等宽排列，占满一行）
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    listOf(
                        Triple(Icons.Filled.PhotoCamera, uiText("拍照", "Camera"), {
                            focusManager.clearFocus(force = true)
                            kbCtrl?.hide()
                            onTakePhoto(); expanded = false
                        }),
                        Triple(Icons.Filled.PhotoLibrary, uiText("图片", "Photos"), {
                            focusManager.clearFocus(force = true)
                            kbCtrl?.hide()
                            onPickImages(); expanded = false
                        }),
                        Triple(Icons.Filled.AttachFile, uiText("文件", "Files"), {
                            focusManager.clearFocus(force = true)
                            kbCtrl?.hide()
                            onPickFiles(); expanded = false
                        })
                    ).forEach { (icon, label, action) ->
                        Surface(
                            onClick = action as () -> Unit,
                            shape = RoundedCornerShape(18.dp),
                            color = ui.surface,
                            border = BorderStroke(1.dp, ui.border),
                            tonalElevation = 0.dp,
                            shadowElevation = 1.dp,
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Column(Modifier.padding(vertical = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(ui.primarySoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, label, Modifier.size(21.dp), tint = ui.primary)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(label, style = MaterialTheme.typography.labelMedium, color = ui.body)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    }

    if (fullGallery) {
        Dialog(
            onDismissRequest = { fullGallery = false; selectedGalleryUris = emptySet(); selectedAlbum = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
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
                    val displayImages = if (selectedAlbum != null) {
                        galleryAlbums.find { it.name == selectedAlbum }?.images ?: emptyList()
                    } else galleryImages
                    if (galleryLoading && displayImages.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            state = galleryGridState,
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
                                        model = coil.request.ImageRequest.Builder(ctx).data(uri).size(260).build(),
                                        contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                    )
                                    if (isSelected) {
                                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                                        Icon(Icons.Filled.Check, "已选", Modifier.align(Alignment.Center).size(32.dp), tint = Color.White)
                                    }
                                }
                            }
                            if (galleryLoading) {
                                item(key = "gallery_loading") {
                                    Box(Modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                    }
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

private fun mergeGalleryAlbums(existing: List<GalleryAlbum>, incoming: List<GalleryAlbum>): List<GalleryAlbum> {
    val albumMap = linkedMapOf<String, MutableList<Uri>>()
    fun addAlbum(album: GalleryAlbum) {
        val list = albumMap.getOrPut(album.name) { mutableListOf() }
        val known = list.map { it.toString() }.toMutableSet()
        album.images.forEach { uri ->
            if (known.add(uri.toString())) list.add(uri)
        }
    }
    existing.forEach(::addAlbum)
    incoming.forEach(::addAlbum)
    return albumMap.mapNotNull { (name, images) ->
        images.firstOrNull()?.let { cover -> GalleryAlbum(name = name, coverUri = cover, images = images) }
    }.sortedByDescending { it.images.size }
}

private fun buildGalleryResult(albumMap: LinkedHashMap<String, MutableList<Uri>>, allImages: List<Uri>): Pair<List<Uri>, List<GalleryAlbum>> {
    val albums = albumMap.mapNotNull { (name, images) ->
        images.firstOrNull()?.let { cover -> GalleryAlbum(name = name, coverUri = cover, images = images) }
    }.sortedByDescending { it.images.size }
    return allImages to albums
}

private fun readGalleryCursor(cursor: android.database.Cursor, limit: Int, skip: Int): Pair<List<Uri>, List<GalleryAlbum>> {
    val allImages = mutableListOf<Uri>()
    val albumMap = linkedMapOf<String, MutableList<Uri>>()
    val idCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
    val bucketCol = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
    var skipped = 0
    while (skipped < skip && cursor.moveToNext()) {
        skipped++
    }
    var count = 0
    while (cursor.moveToNext() && count < limit) {
        val id = cursor.getLong(idCol)
        val bucket = if (bucketCol >= 0) cursor.getString(bucketCol) ?: "其他" else "其他"
        val uri = android.content.ContentUris.withAppendedId(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        allImages.add(uri)
        albumMap.getOrPut(bucket) { mutableListOf() }.add(uri)
        count++
    }
    return buildGalleryResult(albumMap, allImages)
}

/** 从 MediaStore 查询图片，按相册分组 */
private fun queryGalleryImages(context: android.content.Context, limit: Int = 200, offset: Int = 0): Pair<List<Uri>, List<GalleryAlbum>> {
    val safeLimit = limit.coerceAtLeast(0)
    val safeOffset = offset.coerceAtLeast(0)
    if (safeLimit == 0) return emptyList<Uri>() to emptyList()

    val projection = arrayOf(
        android.provider.MediaStore.Images.Media._ID,
        android.provider.MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )
    val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_MODIFIED} DESC"
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val queryArgs = android.os.Bundle().apply {
                putStringArray(
                    android.content.ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(android.provider.MediaStore.Images.Media.DATE_MODIFIED)
                )
                putInt(
                    android.content.ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    android.content.ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
                putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, safeLimit)
                putInt(android.content.ContentResolver.QUERY_ARG_OFFSET, safeOffset)
            }
            val pagedCursor = try {
                context.contentResolver.query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    queryArgs,
                    null
                )
            } catch (_: Exception) {
                null
            }
            if (pagedCursor != null) {
                return pagedCursor.use { readGalleryCursor(it, safeLimit, skip = 0) }
            }
        }

        context.contentResolver.query(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            readGalleryCursor(cursor, safeLimit, skip = safeOffset)
        } ?: (emptyList<Uri>() to emptyList())
    } catch (_: Exception) {
        emptyList<Uri>() to emptyList()
    }
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
                            Icon(Icons.Filled.StopCircle, "停止", tint = MaterialTheme.colorScheme.error)
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
                SheetActionButton(icon = Icons.Filled.PhotoCamera, label = "拍照", onClick = onTakePhoto)
                SheetActionButton(icon = Icons.Filled.PhotoLibrary, label = "图片", onClick = onPickImages)
                SheetActionButton(icon = Icons.Filled.AttachFile, label = "文件", onClick = onPickFiles)
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
    val ui = appUiColors()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(18.dp),
            color = ui.surface,
            border = BorderStroke(1.dp, ui.border),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(ui.primarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, label, Modifier.size(22.dp), tint = ui.primary)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = ui.body)
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
    onRename: (String, String) -> Unit,
    uiText: (String, String) -> String = { zh, _ -> zh },
    onSettings: () -> Unit
) {
    val ui = appUiColors()
    val context = LocalContext.current
    val historyGroups = remember(sessions) { groupSessionsForDrawer(sessions) }
    var renameTarget by remember { mutableStateOf<com.aiaggregator.app.data.model.Session?>(null) }
    var renameText by remember { mutableStateOf("") }
    var batchMode by remember { mutableStateOf(false) }
    var selectedSessionIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val placeholderToast = { label: String ->
        Toast.makeText(context, "$label 功能预留中", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(sessions) {
        val existingIds = sessions.map { it.id }.toSet()
        selectedSessionIds = selectedSessionIds.filter { it in existingIds }.toSet()
        if (sessions.isEmpty()) batchMode = false
    }

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp).fillMaxHeight(),
        drawerContainerColor = ui.background,
        drawerContentColor = ui.text
    ) {
        Row(
            Modifier.fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 22.dp, end = 18.dp, top = 18.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo_transparent),
                contentDescription = "清畅AI",
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "清畅AI",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = ui.text,
                maxLines = 1
            )
            DrawerRoundIconButton(
                icon = Icons.Filled.Search,
                contentDescription = "搜索",
                onClick = { placeholderToast("搜索历史") }
            )
        }

        if (!batchMode) {
            Surface(
                Modifier.fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .clickable { onNew() },
                shape = RoundedCornerShape(18.dp),
                color = ui.panel,
                border = BorderStroke(1.dp, Color.Transparent)
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AddCircleOutline, null, Modifier.size(23.dp), tint = ui.body)
                    Spacer(Modifier.width(12.dp))
                    Text(uiText("新建对话", "New chat"), style = MaterialTheme.typography.titleMedium, color = ui.text)
                }
            }

            Spacer(Modifier.height(14.dp))
            DrawerFeatureRow(
                icon = Icons.Filled.FolderOpen,
                title = uiText("我的空间", "My space"),
                trailing = true,
                onClick = { placeholderToast("我的空间") }
            )
            DrawerFeatureRow(
                icon = Icons.Filled.SmartToy,
                title = uiText("智能体", "Agents"),
                trailing = true,
                onClick = { placeholderToast("智能体") }
            )

            HorizontalDivider(
                Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 12.dp),
                color = ui.border.copy(alpha = 0.75f)
            )
        } else {
            Spacer(Modifier.height(34.dp))
        }

        if (historyGroups.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(uiText("今天", "Today"), style = MaterialTheme.typography.labelLarge, color = ui.placeholder)
                Spacer(Modifier.height(14.dp))
                Text(uiText("还没有历史对话", "No chat history yet"), style = MaterialTheme.typography.bodyMedium, color = ui.subText)
                Text(uiText("新建一个问题后会显示在这里", "Start a chat and it will appear here"), style = MaterialTheme.typography.bodySmall, color = ui.placeholder)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                historyGroups.forEach { group ->
                    item(key = "section-${group.label}") {
                        Text(
                            when (group.label) {
                                "今天" -> uiText("今天", "Today")
                                "最近一周" -> uiText("最近一周", "Last 7 days")
                                "更早" -> uiText("更早", "Earlier")
                                else -> group.label
                            },
                            modifier = Modifier.padding(start = 24.dp, top = 10.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = ui.placeholder
                        )
                    }
                    items(group.sessions, key = { it.id }) { s ->
                        val toggleSelection = {
                            selectedSessionIds = if (s.id in selectedSessionIds) {
                                selectedSessionIds - s.id
                            } else {
                                selectedSessionIds + s.id
                            }
                        }
                        DrawerHistoryRow(
                            title = s.title,
                            selected = s.id == currentId,
                            batchMode = batchMode,
                            checked = s.id in selectedSessionIds,
                            onClick = { if (batchMode) toggleSelection() else onSelect(s.id) },
                            onCheckedChange = toggleSelection,
                            onBatchManage = {
                                batchMode = true
                                selectedSessionIds = setOf(s.id)
                            },
                            onRename = {
                                renameTarget = s
                                renameText = s.title
                            },
                            onDelete = { onDelete(s.id) }
                        )
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(horizontal = 22.dp), color = ui.border.copy(alpha = 0.75f))

        if (batchMode) {
            DrawerBatchActionBar(
                selectedCount = selectedSessionIds.size,
                totalCount = sessions.size,
                onCancel = {
                    batchMode = false
                    selectedSessionIds = emptySet()
                },
                onDelete = {
                    selectedSessionIds.forEach { onDelete(it) }
                    selectedSessionIds = emptySet()
                    batchMode = false
                }
            )
        } else {
            Row(
                Modifier.fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { placeholderToast("账号登录") }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(50),
                        color = ui.primarySoft,
                        border = BorderStroke(1.dp, ui.primaryBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.app_logo_transparent),
                                contentDescription = "用户头像",
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            uiText("未登录", "Not signed in"),
                            style = MaterialTheme.typography.titleMedium,
                            color = ui.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(uiText("点击登录账号", "Tap to sign in"), style = MaterialTheme.typography.bodySmall, color = ui.subText)
                    }
                }
                DrawerRoundIconButton(
                    icon = Icons.Filled.Notifications,
                    contentDescription = "通知",
                    onClick = { placeholderToast("通知") }
                )
                Spacer(Modifier.width(4.dp))
                DrawerRoundIconButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = "设置",
                    onClick = onSettings
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }

    val target = renameTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("修改标题") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("对话标题") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(target.id, renameText)
                        renameTarget = null
                    },
                    enabled = renameText.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            }
        )
    }

}

@Composable
private fun DrawerBatchActionBar(
    selectedCount: Int,
    totalCount: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val ui = appUiColors()
    Row(
        Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "取消",
            modifier = Modifier.clickable { onCancel() }.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = ui.text
        )
        Spacer(Modifier.weight(1f))
        Text(
            "删除 $selectedCount/$totalCount",
            modifier = Modifier
                .clickable(enabled = selectedCount > 0) { onDelete() }
                .padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error.copy(alpha = if (selectedCount > 0) 1f else 0.45f)
        )
    }
}

@Composable
private fun DrawerBatchCheckMark(
    checked: Boolean,
    onClick: () -> Unit
) {
    val ui = appUiColors()
    Surface(
        modifier = Modifier.size(32.dp).clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = if (checked) ui.primary else Color.Transparent,
        border = BorderStroke(2.dp, if (checked) ui.primary else ui.border)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (checked) {
                Icon(Icons.Filled.Check, "已选择", Modifier.size(21.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun DrawerFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailing: Boolean,
    onClick: () -> Unit
) {
    val ui = appUiColors()
    Row(
        Modifier.fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(27.dp), tint = ui.body)
        Spacer(Modifier.width(16.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = ui.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (trailing) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(25.dp), tint = ui.iconMuted)
        }
    }
}

@Composable
private fun DrawerHistoryRow(
    title: String,
    selected: Boolean,
    batchMode: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
    onCheckedChange: () -> Unit,
    onBatchManage: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val ui = appUiColors()
    var menuExpanded by remember { mutableStateOf(false) }
    Surface(
        Modifier.fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 1.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if ((batchMode && checked) || (!batchMode && selected)) ui.panel else ui.background
    ) {
        Row(
            Modifier.padding(start = 10.dp, end = 6.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.width(4.dp).height(30.dp)
                    .background(
                        if (!batchMode && selected) ui.primary else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                title,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if ((batchMode && checked) || (!batchMode && selected)) FontWeight.SemiBold else FontWeight.Normal,
                color = ui.text
            )
            if (batchMode) {
                DrawerBatchCheckMark(checked = checked, onClick = onCheckedChange)
            } else {
                Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.MoreHoriz,
                        "更多操作",
                        Modifier.size(22.dp),
                        tint = ui.iconMuted
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    shape = RoundedCornerShape(22.dp),
                    containerColor = ui.surface
                ) {
                    DrawerHistoryMenuItem(
                        icon = Icons.Filled.Check,
                        text = "批量管理",
                        onClick = {
                            menuExpanded = false
                            onBatchManage()
                        }
                    )
                    DrawerHistoryMenuItem(
                        icon = Icons.Filled.Edit,
                        text = "修改标题",
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DrawerHistoryMenuItem(
                        icon = Icons.Filled.Delete,
                        text = "删除对话",
                        danger = true,
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
                }
            }
        }
    }
}

@Composable
private fun DrawerHistoryMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val ui = appUiColors()
    val color = if (danger) MaterialTheme.colorScheme.error else ui.text
    DropdownMenuItem(
        text = {
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        },
        leadingIcon = {
            Icon(
                icon,
                null,
                Modifier.size(24.dp),
                tint = color
            )
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
    )
}

@Composable
private fun DrawerRoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val ui = appUiColors()
    Surface(
        modifier = Modifier.size(42.dp).clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, Modifier.size(25.dp), tint = ui.text)
        }
    }
}

private data class DrawerHistoryGroup(
    val label: String,
    val sessions: List<com.aiaggregator.app.data.model.Session>
)

private fun groupSessionsForDrawer(
    sessions: List<com.aiaggregator.app.data.model.Session>
): List<DrawerHistoryGroup> {
    if (sessions.isEmpty()) return emptyList()
    val todayStart = startOfTodayMillis()
    val weekStart = todayStart - 6L * 24L * 60L * 60L * 1000L
    val sorted = sessions.sortedByDescending { it.lastActiveAt }
    return listOf(
        DrawerHistoryGroup("今天", sorted.filter { it.lastActiveAt >= todayStart }),
        DrawerHistoryGroup("最近一周", sorted.filter { it.lastActiveAt in weekStart until todayStart }),
        DrawerHistoryGroup("更早", sorted.filter { it.lastActiveAt < weekStart })
    ).filter { it.sessions.isNotEmpty() }
}

private fun startOfTodayMillis(): Long {
    return java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
}

// ── Config ──

private fun ModelConfig.displayLabel(): String = displayName.ifBlank { modelName }

private fun configFeedbackLabel(p: com.aiaggregator.app.data.model.ApiConfig?, m: ModelConfig): String {
    val platformLabel = p?.platformName?.ifBlank { p.baseUrl } ?: "未知平台"
    return "$platformLabel / ${m.displayLabel()}"
}

private fun ModelCategory.sortRank(): Int = when (this) {
    ModelCategory.CHAT -> 0
    ModelCategory.MULTIMODAL -> 1
    ModelCategory.IMAGE -> 2
    ModelCategory.VIDEO -> 3
    ModelCategory.AUDIO -> 4
}

private fun ModelCategory.shortMark(): String = when (this) {
    ModelCategory.CHAT -> "文"
    ModelCategory.MULTIMODAL -> "多"
    ModelCategory.IMAGE -> "图"
    ModelCategory.VIDEO -> "视"
    ModelCategory.AUDIO -> "音"
}

private fun ModelCategory.accentColor(): Color = when (this) {
    ModelCategory.CHAT -> AppPrimary
    ModelCategory.MULTIMODAL -> Color(0xFF2F5FD8)
    ModelCategory.IMAGE -> Color(0xFF0E83D8)
    ModelCategory.VIDEO -> Color(0xFF4E6BE6)
    ModelCategory.AUDIO -> AppMuted
}

private data class Vendor(val id: String, val label: String, val baseUrl: String, val formatType: ApiFormatType)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(
    vm: ConfigViewModel,
    refreshKey: Int,
    onAddModel: () -> Unit,
    onEditModel: (String) -> Unit,
    onConfigChanged: () -> Unit
) {
    val configState by vm.state.collectAsState()
    val models = remember(configState.models) {
        configState.models.sortedWith(
            compareBy<ModelConfig> { it.category.sortRank() }
                .thenByDescending { it.isDefault }
                .thenByDescending { it.showInPicker }
                .thenBy { it.displayLabel() }
        )
    }
    val platformsById = remember(configState.platforms) { configState.platforms.associateBy { it.id } }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<ModelCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val ctxToast = LocalContext.current
    val scope = rememberCoroutineScope()
    val visiblePickerCount = models.count { it.showInPicker }
    val defaultModel = models.find { it.isDefault && it.category != ModelCategory.IMAGE }
    val availableCategories = remember(models) {
        models.map { it.category }.distinct().sortedBy { it.sortRank() }
    }
    val filteredModels = remember(models, selectedCategory, searchQuery) {
        val query = searchQuery.trim()
        models.filter { model ->
            val categoryMatched = selectedCategory == null || model.category == selectedCategory
            val queryMatched = query.isBlank() ||
                model.displayLabel().contains(query, ignoreCase = true) ||
                model.modelName.contains(query, ignoreCase = true) ||
                platformsById[model.platformId]?.platformName.orEmpty().contains(query, ignoreCase = true)
            categoryMatched && queryMatched
        }
    }

    LaunchedEffect(refreshKey) {
        vm.refresh()
    }

    LaunchedEffect(models, selectedCategory) {
        val category = selectedCategory
        if (category != null && models.none { it.category == category }) {
            selectedCategory = null
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(AppBg)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFEFF5FF), Color(0xFFFFFFFF), Color(0xFFF7FAFF))
                    )
                )
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        "模型管理",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppText
                    )
                    Text(
                        "集中管理与配置你的 AI 模型资源",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppSubText
                    )
                }
                Surface(
                    modifier = Modifier.clickable { onAddModel() },
                    shape = RoundedCornerShape(16.dp),
                    color = AppPrimary,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, null, Modifier.size(18.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("新增", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }

        if (models.isEmpty()) {
            ElevatedCard(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.AddCircle, null, Modifier.size(48.dp), tint = AppPrimary)
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfigCategoryTab(
                    text = "全部",
                    count = models.size,
                    selected = selectedCategory == null,
                    color = AppPrimary,
                    onClick = { selectedCategory = null }
                )
                availableCategories.forEach { category ->
                    ConfigCategoryTab(
                        text = category.label,
                        count = models.count { it.category == category },
                        selected = selectedCategory == category,
                        color = category.accentColor(),
                        onClick = { selectedCategory = category }
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = {
                    Icon(Icons.Filled.Search, null, Modifier.size(20.dp), tint = AppMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        Icon(
                            Icons.Filled.Close,
                            null,
                            Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" },
                            tint = AppMuted
                        )
                    }
                },
                placeholder = { Text("搜索模型名称", color = AppPlaceholder) }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfigMetricPill("共 ${models.size} 个模型", AppPrimary)
                ConfigMetricPill("前台显示 $visiblePickerCount", AppPrimary)
                ConfigMetricPill("默认 ${defaultModel?.displayLabel() ?: "未设置"}", AppMuted)
            }

            if (filteredModels.isEmpty()) {
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = AppSurface
                ) {
                    Text(
                        "这个分类里暂时没有模型。",
                        Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppSubText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            filteredModels.forEach { m ->
                val p = platformsById[m.platformId]
                val accent = m.category.accentColor()
                ElevatedCard(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = AppSurface)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = accent.copy(alpha = 0.14f)
                            ) {
                                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        m.category.shortMark(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accent
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        m.displayName.ifBlank { m.modelName },
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    m.modelName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppSubText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ConfigStatusPill(m.category.label, color = accent, strong = false)
                                    ConfigStatusPill(if (m.showInPicker) "前台显示" else "未用于前台", color = AppPrimary, strong = m.showInPicker)
                                    if (m.isDefault && m.category != ModelCategory.IMAGE) {
                                        ConfigStatusPill("默认", color = AppPrimary, strong = true)
                                    }
                                    ConfigStatusPill(if (p == null) "缺平台" else "运行中", color = if (p == null) MaterialTheme.colorScheme.error else AppPrimary, strong = p != null)
                                }
                            }
                            Icon(Icons.Filled.Menu, null, Modifier.size(20.dp), tint = AppIconMuted)
                        }
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = AppPanel
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    p?.platformName?.ifBlank { "未知平台" } ?: "未知平台",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppBody
                                )
                                if (p != null) {
                                    Text(
                                        p.baseUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppSubText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = AppBorder)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            var testing by remember { mutableStateOf(false) }
                            ConfigActionButton(
                                icon = Icons.Filled.Refresh,
                                text = if (testing) "测试中" else "测试",
                                enabled = !testing,
                                onClick = {
                                    if (testing) return@ConfigActionButton
                                    if (p == null) {
                                        Toast.makeText(ctxToast, "测试失败：缺少平台配置", Toast.LENGTH_SHORT).show()
                                        return@ConfigActionButton
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
                                }
                            )
                            ConfigActionButton(
                                icon = Icons.Filled.Refresh,
                                text = if (m.showInPicker) "当前前台" else "设为前台",
                                selected = m.showInPicker,
                                onClick = {
                                    if (m.showInPicker) {
                                        Toast.makeText(ctxToast, "每个类型只能有一个前台模型，当前${m.category.label}已经是：${m.displayLabel()}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val replaced = models.firstOrNull {
                                            it.category == m.category && it.showInPicker && it.id != m.id
                                        }
                                        scope.launch {
                                            val ok = vm.setCategoryPickerModel(m.id)
                                            if (ok) {
                                                onConfigChanged()
                                                val message = if (replaced != null) {
                                                    "每个类型只能有一个，已用 ${m.displayLabel()} 顶替 ${replaced.displayLabel()}"
                                                } else {
                                                    "已设为${m.category.label}的前台模型：${m.displayLabel()}"
                                                }
                                                Toast.makeText(
                                                    ctxToast,
                                                    message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            )
                            ConfigActionButton(
                                icon = Icons.Filled.Check,
                                text = if (m.category == ModelCategory.IMAGE) "不可默认" else if (m.isDefault) "已默认" else "设默认",
                                selected = m.isDefault && m.category != ModelCategory.IMAGE,
                                onClick = {
                                    val feedbackLabel = configFeedbackLabel(p, m)
                                    when {
                                        m.category == ModelCategory.IMAGE -> Toast.makeText(ctxToast, "图片生成模型不能设为默认聊天模型：${feedbackLabel}", Toast.LENGTH_SHORT).show()
                                        m.isDefault -> Toast.makeText(ctxToast, "已是默认模型：${feedbackLabel}", Toast.LENGTH_SHORT).show()
                                        else -> {
                                            scope.launch {
                                                val ok = vm.setDefaultModel(m.id)
                                                if (ok) {
                                                    onConfigChanged()
                                                    Toast.makeText(ctxToast, "已将 ${feedbackLabel} 设为默认模型", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(ctxToast, "图片生成模型不能设为默认聊天模型：${feedbackLabel}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                            ConfigActionButton(Icons.Filled.Edit, "编辑") { onEditModel(m.id) }
                            ConfigActionButton(
                                icon = Icons.Filled.ContentCopy,
                                text = "复制",
                                onClick = {
                                    if (p == null) {
                                        Toast.makeText(ctxToast, "复制失败：缺少平台配置", Toast.LENGTH_SHORT).show()
                                    } else {
                                        scope.launch {
                                            val copied = vm.duplicateModel(m.id)
                                            if (copied == null) {
                                                Toast.makeText(ctxToast, "复制失败：缺少平台配置", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val (newModel, _) = copied
                                                onConfigChanged()
                                                Toast.makeText(ctxToast, "已复制 ${configFeedbackLabel(p, m)} 为 ${newModel.displayLabel()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            )
                            ConfigActionButton(Icons.Filled.Delete, "删除", danger = true) { deleteConfirmId = m.id }
                        }
                    }
                }
            }
        }

        if (deleteConfirmId != null) {
            val modelToDelete = models.find { it.id == deleteConfirmId }
            val platformToDelete = modelToDelete?.let { platformsById[it.platformId] }
            val deleteLabel = modelToDelete?.let { configFeedbackLabel(platformToDelete, it) }.orEmpty()
            AlertDialog(
                onDismissRequest = { deleteConfirmId = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除「${deleteLabel}」吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(onClick = {
                        val targetId = deleteConfirmId
                        if (targetId != null) {
                            scope.launch {
                                vm.deleteModel(targetId)
                                deleteConfirmId = null
                                onConfigChanged()
                                Toast.makeText(ctxToast, "已删除 ${deleteLabel}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { deleteConfirmId = null }) { Text("取消") } },
                shape = RoundedCornerShape(20.dp)
            )
        }

    }
}

@Composable
private fun ConfigCategoryTab(
    text: String,
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.12f) else AppSurface,
        border = BorderStroke(1.dp, if (selected) color.copy(alpha = 0.65f) else AppBorder)
    ) {
        Text(
            "$text $count",
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) color else AppBody
        )
    }
}

@Composable
private fun ConfigMetricPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = AppSurface,
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
            Spacer(Modifier.width(7.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = AppBody, maxLines = 1)
        }
    }
}

@Composable
private fun ConfigStatusPill(text: String, color: Color, strong: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (strong) color.copy(alpha = 0.11f) else AppPanel,
        border = BorderStroke(1.dp, if (strong) color.copy(alpha = 0.22f) else AppBorder)
    ) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (strong) {
                Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = if (strong) color else AppMuted
            )
        }
    }
}

@Composable
private fun ConfigActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean = true,
    selected: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val color = when {
        danger -> MaterialTheme.colorScheme.error
        selected -> AppPrimary
        else -> AppBody
    }
    Surface(
        modifier = Modifier.clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) AppPrimarySoft else AppSurface,
        border = BorderStroke(1.dp, if (selected) AppPrimaryBorder else AppBorder)
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(14.dp), tint = color.copy(alpha = if (enabled) 1f else 0.45f))
            Spacer(Modifier.width(5.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = if (enabled) 1f else 0.45f)
            )
        }
    }
}
