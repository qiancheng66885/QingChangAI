package com.aiaggregator.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.aiaggregator.app.data.local.SettingsStore
import com.aiaggregator.app.data.model.AppLanguage
import com.aiaggregator.app.data.model.ThemeMode
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(onNavConfig: () -> Unit, onNavData: () -> Unit, onNavAbout: () -> Unit, onNavSupport: () -> Unit = {}) {
    val ctx = LocalContext.current
    val store = remember { SettingsStore(ctx) }
    val settings by store.settingsFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    // Bilingual helper
    fun t(zh: String, en: String): String = when (settings?.language) {
        AppLanguage.ENGLISH -> en
        AppLanguage.CHINESE -> zh
        else -> if (java.util.Locale.getDefault().language == "zh") zh else en
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Appearance ──
        SectionHeader(t("外观", "Appearance"), Icons.Filled.ColorLens)
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(t("主题模式", "Theme"), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    when (settings?.themeMode) {
                        ThemeMode.LIGHT -> t("浅色模式", "Light")
                        ThemeMode.DARK -> t("深色模式", "Dark")
                        else -> t("跟随系统", "System")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.LIGHT -> t("浅色", "Light")
                            ThemeMode.DARK -> t("深色", "Dark")
                            ThemeMode.SYSTEM -> t("系统", "System")
                        }
                        FilterChip(
                            selected = settings?.themeMode == mode,
                            onClick = {
                                scope.launch {
                                    try {
                                        if (settings?.themeMode == mode) {
                                            Toast.makeText(ctx, t("已选择：$label", "Selected: $label"), Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        store.setThemeMode(mode)
                                        Toast.makeText(ctx, t("主题已切换为：$label", "Theme changed to: $label"), Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, t("主题切换失败：${e.message}", "Theme change failed: ${e.message}"), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text(t("语言", "Language"), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        val label = when (lang) {
                            AppLanguage.CHINESE -> "中文"
                            AppLanguage.ENGLISH -> "English"
                            AppLanguage.SYSTEM -> t("系统", "System")
                        }
                        FilterChip(
                            selected = settings?.language == lang,
                            onClick = {
                                scope.launch {
                                    try {
                                        if (settings?.language == lang) {
                                            Toast.makeText(ctx, t("已选择：$label", "Selected: $label"), Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        store.setLanguage(lang)
                                        Toast.makeText(ctx, t("语言已切换为：$label", "Language changed to: $label"), Toast.LENGTH_SHORT).show()
                                        applyLanguage(ctx, lang)
                                        (ctx as? Activity)?.recreate()
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, t("语言切换失败：${e.message}", "Language change failed: ${e.message}"), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // ── Features ──
        SectionHeader(t("功能", "Features"), Icons.Filled.RocketLaunch)
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Column {
                NavRow(Icons.Filled.CloudSync, t("API 配置", "API Config"), t("管理平台和模型", "Manage platforms & models")) { onNavConfig() }
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                NavRow(Icons.Filled.DeleteSweep, t("数据管理", "Data"), t("清除缓存和聊天记录", "Clear cache & history")) { onNavData() }
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                NavRow(Icons.Filled.SupportAgent, t("软件支持与教程", "Support"), t("使用帮助和常见问题", "Help & FAQ")) { onNavSupport() }
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                NavRow(Icons.Filled.Info, t("关于", "About"), "${t("版本", "Version")} ${UpdateChecker.getAppVersion()}") { onNavAbout() }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun NavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
    }
}

private fun applyLanguage(ctx: Context, lang: AppLanguage) {
    val locale = when (lang) {
        AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
        AppLanguage.ENGLISH -> Locale.US
        AppLanguage.SYSTEM -> Locale.getDefault()
    }
    if (android.os.Build.VERSION.SDK_INT >= 33) {
        ctx.resources.configuration.let { config ->
            val localeList = LocaleListCompat.create(locale)
            config.setLocale(locale)
        }
    }
    Locale.setDefault(locale)
    val config = Configuration(ctx.resources.configuration)
    config.setLocale(locale)
    @Suppress("DEPRECATION")
    ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics)
}
