package com.aiaggregator.app.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aiaggregator.app.R
import com.aiaggregator.app.base.ext.openUrl
import com.aiaggregator.app.data.local.SettingsStore
import com.aiaggregator.app.data.model.AppLanguage
import com.aiaggregator.app.data.model.ThemeMode
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    onNavConfig: () -> Unit,
    onNavData: () -> Unit,
    onNavAbout: () -> Unit,
    onNavSupport: () -> Unit = {},
    onNavTheme: () -> Unit,
    onNavLanguage: () -> Unit,
    onNavFeature: () -> Unit,
    onNavPermissions: () -> Unit,
    onNavPrivacy: () -> Unit,
    onNavFeedback: () -> Unit
) {
    val ctx = LocalContext.current
    val store = remember { SettingsStore(ctx) }
    val settings by store.settingsFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var checkingUpdate by remember { mutableStateOf(false) }
    val currentTheme = settings?.themeMode ?: ThemeMode.SYSTEM
    val currentLanguage = settings?.language ?: AppLanguage.CHINESE
    val primary = MaterialTheme.colorScheme.primary
    val lang = resolveLanguage(currentLanguage)

    fun t(zh: String, en: String): String = if (lang == AppLanguage.ENGLISH) en else zh

    fun themeLabel(mode: ThemeMode?): String = when (mode) {
        ThemeMode.LIGHT -> t("浅色模式", "Light")
        ThemeMode.DARK -> t("深色模式", "Dark")
        else -> t("跟随系统", "System")
    }

    fun languageLabel(lang: AppLanguage?): String = when (lang) {
        AppLanguage.CHINESE -> "简体中文"
        AppLanguage.ENGLISH -> "English"
        else -> t("跟随系统", "System")
    }

    fun reserved(label: String) {
        Toast.makeText(ctx, t("$label 功能预留", "$label is reserved"), Toast.LENGTH_SHORT).show()
    }

    fun checkUpdate() {
        if (checkingUpdate) return
        checkingUpdate = true
        scope.launch {
            val result = UpdateChecker.check()
            checkingUpdate = false
            when {
                result.error != null -> Toast.makeText(ctx, result.error, Toast.LENGTH_SHORT).show()
                result.hasUpdate -> ctx.openUrl(result.downloadUrl, "无法打开下载页面")
                else -> Toast.makeText(ctx, t("已是最新版本", "Already up to date"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    SettingsPage {
        ProfileCard(
            title = t("未登录", "Not signed in"),
            subtitle = t("登录后启用会员、云同步和账号管理", "Sign in to enable membership, cloud sync and account settings"),
            onClick = { reserved(t("账号登录", "Account sign-in")) }
        )

        SettingsGroupCard {
            SettingsRow(
                icon = Icons.Filled.AccountCircle,
                iconColor = Color(0xFF1F2937),
                title = t("个人资料", "Profile"),
                value = t("登录后同步", "Sign in"),
                onClick = { reserved(t("个人资料", "Profile")) }
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.CloudSync,
                iconColor = primary,
                title = t("云端同步", "Cloud sync"),
                value = t("会员解锁", "Plus"),
                badge = t("预留", "Soon"),
                onClick = { reserved(t("云端同步", "Cloud sync")) }
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Storage,
                iconColor = Color(0xFF4F46E5),
                title = t("自建存储", "Self-hosted storage"),
                value = t("未来支持", "Soon"),
                onClick = { reserved(t("自建存储", "Self-hosted storage")) }
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.FolderOpen,
                iconColor = Color(0xFF0F766E),
                title = t("我的空间", "My space"),
                value = "",
                onClick = { reserved(t("我的空间", "My space")) }
            )
        }

        SectionLabel(t("个性化", "Personalization"))
        SettingsGroupCard {
            SettingsRow(
                icon = Icons.Filled.ColorLens,
                iconColor = primary,
                title = t("主题设置", "Theme"),
                value = themeLabel(currentTheme),
                onClick = onNavTheme
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Translate,
                iconColor = Color(0xFF334155),
                title = t("语言切换", "Language"),
                value = languageLabel(currentLanguage),
                onClick = onNavLanguage
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Search,
                iconColor = Color(0xFF2563EB),
                title = t("联网搜索", "Web search"),
                value = t("待接入", "Soon"),
                badge = t("预留", "Soon"),
                onClick = { reserved(t("联网搜索", "Web search")) }
            )
        }

        SectionLabel(t("通用", "General"))
        SettingsGroupCard {
            SettingsRow(
                icon = Icons.Filled.CloudSync,
                iconColor = primary,
                title = t("模型管理", "Models"),
                value = t("平台和模型", "Providers"),
                onClick = onNavConfig
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Tune,
                iconColor = Color(0xFF0F766E),
                title = t("功能管理", "Features"),
                value = t("能力开关", "Controls"),
                onClick = onNavFeature
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.DeleteSweep,
                iconColor = Color(0xFF2563EB),
                title = t("数据管理", "Data"),
                value = t("缓存与历史", "Cache & history"),
                onClick = onNavData
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.ManageAccounts,
                iconColor = Color(0xFF7C3AED),
                title = t("授权管理", "Authorization"),
                value = t("系统权限", "Permissions"),
                onClick = onNavPermissions
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Security,
                iconColor = Color(0xFF475569),
                title = t("隐私与权限", "Privacy & permissions"),
                value = t("合规说明", "Policy"),
                onClick = onNavPrivacy
            )
        }

        SectionLabel(t("支持", "Support"))
        SettingsGroupCard {
            SettingsRow(
                icon = Icons.Filled.SupportAgent,
                iconColor = Color(0xFF475569),
                title = t("客服中心", "Support center"),
                value = "",
                onClick = onNavSupport
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Feedback,
                iconColor = Color(0xFF2563EB),
                title = t("功能反馈", "Feedback"),
                value = "",
                onClick = onNavFeedback
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Download,
                iconColor = primary,
                title = t("检查更新", "Check for updates"),
                value = if (checkingUpdate) t("检查中", "Checking") else t("当前 ${UpdateChecker.getAppVersion()}", "Current ${UpdateChecker.getAppVersion()}"),
                onClick = { checkUpdate() }
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.Filled.Info,
                iconColor = Color(0xFF475569),
                title = t("关于清畅 AI", "About QingChang AI"),
                value = UpdateChecker.getAppVersion(),
                onClick = onNavAbout
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun ThemeSettingsScreen() {
    val ctx = LocalContext.current
    val store = remember { SettingsStore(ctx) }
    val settings by store.settingsFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val currentTheme = settings?.themeMode ?: ThemeMode.SYSTEM
    val lang = resolveLanguage(settings?.language ?: AppLanguage.CHINESE)
    fun t(zh: String, en: String): String = if (lang == AppLanguage.ENGLISH) en else zh

    SettingsPage {
        SettingsGroupCard {
            val themeModes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
            themeModes.forEachIndexed { index, mode ->
                SelectableRow(
                    title = when (mode) {
                        ThemeMode.SYSTEM -> t("跟随系统", "System")
                        ThemeMode.LIGHT -> t("浅色模式", "Light")
                        ThemeMode.DARK -> t("深色模式", "Dark")
                    },
                    selected = currentTheme == mode,
                    onClick = {
                        scope.launch {
                            if (currentTheme != mode) store.setThemeMode(mode)
                        }
                    }
                )
                if (index != themeModes.lastIndex) GroupDivider(startPadding = 18)
            }
        }
    }
}

@Composable
fun LanguageSettingsScreen() {
    val ctx = LocalContext.current
    val store = remember { SettingsStore(ctx) }
    val settings by store.settingsFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val currentLanguage = settings?.language ?: AppLanguage.CHINESE
    val lang = resolveLanguage(currentLanguage)
    fun t(zh: String, en: String): String = if (lang == AppLanguage.ENGLISH) en else zh

    SettingsPage {
        SettingsGroupCard {
            val languages = listOf(AppLanguage.CHINESE, AppLanguage.ENGLISH, AppLanguage.SYSTEM)
            languages.forEachIndexed { index, lang ->
                SelectableRow(
                    title = when (lang) {
                        AppLanguage.CHINESE -> "简体中文"
                        AppLanguage.ENGLISH -> "English"
                        AppLanguage.SYSTEM -> t("跟随系统", "System")
                    },
                    selected = currentLanguage == lang,
                    onClick = {
                        scope.launch {
                            if (currentLanguage != lang) {
                                store.setLanguage(lang)
                            }
                        }
                    }
                )
                if (index != languages.lastIndex) GroupDivider(startPadding = 18)
            }
        }
    }
}

@Composable
fun PermissionManagementScreen() {
    val ctx = LocalContext.current
    val mediaPermission = if (Build.VERSION.SDK_INT >= 33) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
    }

    SettingsPage {
        SettingsGroupCard {
            PermissionRow(
                icon = Icons.Filled.PhotoCamera,
                title = "相机",
                description = "拍照上传、拍题答疑和图片参考",
                granted = isGranted(android.Manifest.permission.CAMERA),
                onClick = { openAppPermissionSettings(ctx) }
            )
            GroupDivider()
            PermissionRow(
                icon = Icons.Filled.FolderOpen,
                title = "照片和图片",
                description = "选择图片、上传参考图和保存生成图",
                granted = isGranted(mediaPermission),
                onClick = { openAppPermissionSettings(ctx) }
            )
            GroupDivider()
            PermissionRow(
                icon = Icons.Filled.CloudSync,
                title = "网络",
                description = "请求模型、图片生成、检查更新和加载远程资源",
                granted = true,
                onClick = { Toast.makeText(ctx, "网络权限为安装时声明权限，无需单独授权", Toast.LENGTH_SHORT).show() }
            )
        }

        SettingsGroupCard {
            SettingsRow(
                icon = Icons.Filled.Settings,
                iconColor = MaterialTheme.colorScheme.primary,
                title = "打开系统权限设置",
                value = "",
                onClick = { openAppPermissionSettings(ctx) }
            )
        }
    }
}

@Composable
fun PrivacyPermissionScreen() {
    SettingsPage {
        SettingsGroupCard {
            InfoRow(
                icon = Icons.Filled.Security,
                title = "权限使用说明",
                body = "相机和图片权限只在你主动拍照、选择图片、上传附件或保存生成图片时使用。"
            )
            GroupDivider()
            InfoRow(
                icon = Icons.Filled.Lock,
                title = "本地数据",
                body = "聊天记录、消息和生成图片优先保存在本机；模型 API Key 使用本地加密存储。"
            )
            GroupDivider()
            InfoRow(
                icon = Icons.Filled.CloudSync,
                title = "第三方模型服务",
                body = "你配置的模型平台会收到你发送的文本、图片和必要请求参数；不同平台的数据处理规则以其服务条款为准。"
            )
        }

        SettingsGroupCard {
            InfoRow(
                icon = Icons.Filled.Info,
                title = "隐私政策",
                body = "正式上架前需要补齐可访问的隐私政策、用户协议、个人信息收集清单和第三方服务清单。"
            )
            GroupDivider()
            InfoRow(
                icon = Icons.Filled.ManageAccounts,
                title = "账号与数据删除",
                body = "当前账号体系未上线；未来支持登录、会员或云同步后，需要补齐注销账号和删除云端数据入口。"
            )
        }
    }
}

@Composable
fun FeatureManagementScreen() {
    SettingsPage {
        SettingsGroupCard {
            InfoRow(
                icon = Icons.Filled.Search,
                title = "联网搜索",
                body = "未来接入真实搜索工具或模型 citations 后，可在这里统一控制。"
            )
            GroupDivider()
            InfoRow(
                icon = Icons.Filled.ColorLens,
                title = "外观能力",
                body = "主题、语言和文字大小会逐步收拢到个性化设置中。"
            )
            GroupDivider()
            InfoRow(
                icon = Icons.Filled.Storage,
                title = "图片与文件",
                body = "后续可管理自动保存图片、附件上传、诊断日志和缓存策略。"
            )
        }
    }
}

@Composable
fun FeedbackScreen() {
    SettingsPage {
        SettingsGroupCard {
            InfoRow(
                icon = Icons.Filled.Feedback,
                title = "功能改进建议",
                body = "可以反馈模型配置、图片生成、联网搜索、界面交互和上架合规相关建议。"
            )
            GroupDivider()
            InfoRow(
                icon = Icons.Filled.SupportAgent,
                title = "问题反馈",
                body = "遇到崩溃、卡顿、生成失败或中转站兼容问题时，建议附上机型、系统版本、模型平台和复现步骤。"
            )
            GroupDivider()
            InfoRow(
                icon = Icons.Filled.Info,
                title = "后续接入",
                body = "正式版本建议接入邮件、在线表单或客服系统；当前先作为 App 内入口和说明页。"
            )
        }
    }
}

@Composable
private fun SettingsPage(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content
        )
    }
}

@Composable
private fun ProfileCard(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(62.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.app_logo_transparent),
                        contentDescription = "清畅AI",
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        Modifier.padding(start = 4.dp, bottom = 0.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    badge: String = "",
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(28.dp), tint = iconColor)
        Spacer(Modifier.width(18.dp))
        Text(
            title,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (badge.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ) {
                Text(
                    badge,
                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        if (value.isNotBlank()) {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun SelectableRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (selected) {
            Icon(
                Icons.Filled.Check,
                null,
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            if (granted) "已允许" else "未允许",
            style = MaterialTheme.typography.bodyMedium,
            color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, body: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GroupDivider(startPadding: Int = 64) {
    HorizontalDivider(
        Modifier.padding(start = startPadding.dp),
        thickness = 0.6.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
    )
}

private fun openAppPermissionSettings(ctx: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", ctx.packageName, null)
    )
    ctx.startActivity(intent)
}

private fun resolveLanguage(lang: AppLanguage): AppLanguage {
    return when (lang) {
        AppLanguage.SYSTEM -> if (Locale.getDefault().language == "zh") AppLanguage.CHINESE else AppLanguage.ENGLISH
        else -> lang
    }
}
