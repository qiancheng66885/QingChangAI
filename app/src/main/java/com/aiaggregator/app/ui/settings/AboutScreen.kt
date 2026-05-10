package com.aiaggregator.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aiaggregator.app.R

@Composable
fun AboutScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp))) { Image(painter = painterResource(R.drawable.bjlg), contentDescription = null, modifier = Modifier.fillMaxSize()); Image(painter = painterResource(R.drawable.qjlg), contentDescription = "清畅AI", modifier = Modifier.fillMaxSize()) }

        Spacer(Modifier.height(20.dp))
        Text("清畅AI", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text("版本 1.0.0 · 开源免费", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
            Text("作者：迁城", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }

        Spacer(Modifier.height(32.dp))

        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors()
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
            colors = CardDefaults.elevatedCardColors()
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
        val ctx = LocalContext.current
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("下载更新", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "当前版本 1.0.0。新版本发布后可到以下开源地址下载最新安装包。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                // GitHub
                Surface(
                    Modifier.fillMaxWidth().clickable {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/qiancheng66885/QingChangAI")))
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("GitHub", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Text("github.com/qiancheng66885/QingChangAI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Gitee
                Surface(
                    Modifier.fillMaxWidth().clickable {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://gitee.com/qiancheng2025/QingChangAI")))
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
