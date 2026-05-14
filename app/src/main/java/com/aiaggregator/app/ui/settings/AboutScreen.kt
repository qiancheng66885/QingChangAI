package com.aiaggregator.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aiaggregator.app.R
import com.aiaggregator.app.base.ext.openUrl
import kotlinx.coroutines.launch

@Composable
fun AboutScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(UpdateChecker.Result()) }
    var checking by remember { mutableStateOf(false) }

    fun openLink(url: String, failureMessage: String = "无法打开链接") {
        ctx.openUrl(url, failureMessage)
    }

    fun doCheck() {
        if (checking) return
        checking = true
        scope.launch {
            val result = UpdateChecker.check()
            checking = false
            if (result.error != null) {
                Toast.makeText(ctx, result.error, Toast.LENGTH_SHORT).show()
            } else if (result.hasUpdate) {
                showUpdateDialog = result
            } else {
                Toast.makeText(ctx, "已是最新版本", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Update dialog
    if (showUpdateDialog.hasUpdate) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = UpdateChecker.Result() },
            title = { Text("发现新版本") },
            text = {
                Text("最新版本：${showUpdateDialog.latestVersion}\n当前版本：${UpdateChecker.getAppVersion()}\n\n请到开源地址下载最新安装包。")
            },
            confirmButton = {
                TextButton(onClick = {
                    openLink(showUpdateDialog.downloadUrl, "无法打开下载页面")
                    showUpdateDialog = UpdateChecker.Result()
                }) { Text("去下载") }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = UpdateChecker.Result() }) { Text("稍后") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Image(
            painter = painterResource(R.drawable.app_logo_transparent),
            contentDescription = "清畅AI",
            modifier = Modifier.size(96.dp)
        )

        Spacer(Modifier.height(20.dp))
        Text("清畅AI", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))

        // Version + check update
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("版本 ${UpdateChecker.getAppVersion()} · 开源免费", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Surface(
                modifier = Modifier.clickable { doCheck() },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    if (checking) "检查中..." else "检查更新",
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Text("作者：迁城", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(32.dp))

        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                InfoRow("平台", "Android")
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                InfoRow("技术栈", "Kotlin + Jetpack Compose")
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                InfoRow("设计系统", "Material 3")
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                InfoRow("许可协议", "开源免费")
            }
        }

        Spacer(Modifier.height(24.dp))

        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("关于本项目", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "AI 聚合是一款开源免费的 Android 客户端，初衷是让更多人能够借助中转站或第三方 API 服务，方便地调用各种最新 AI 模型，无需自行搭建复杂的基础设施。\n\n支持 OpenAI 兼容格式和 Anthropic 兼容格式，数据全本地加密存储，无广告、无付费、无追踪。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // 下载更新
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("下载更新", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "为了防止有时候出现无法更新、下载失败或版本异常等情况，可以到我的开源 GitHub 下载最新软件。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    Modifier.fillMaxWidth().clickable {
                        openLink("https://github.com/qiancheng66885/QingChangAI")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("GitHub", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Text("github.com/qiancheng66885/QingChangAI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    Modifier.fillMaxWidth().clickable {
                        openLink("https://gitee.com/qiancheng2025/QingChangAI")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Gitee", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Text("gitee.com/qiancheng2025/QingChangAI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
