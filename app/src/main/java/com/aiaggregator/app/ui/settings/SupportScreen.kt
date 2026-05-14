package com.aiaggregator.app.ui.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiaggregator.app.base.ext.openUrl

@Composable
fun SupportScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 使用教程
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("使用教程", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                TutorialItem("1", "添加平台", "设置 → API 配置 → 添加模型，选择 OpenAI 或 Anthropic 兼容格式，填入 API 地址和密钥。")
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                TutorialItem("2", "开始对话", "选择模型 → 输入文字 → 发送。支持流式输出、多轮对话。")
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                TutorialItem("3", "图片生成", "切换到图片模型 → 输入描述 → 自动生成。可点击图片放大查看、保存到相册。")
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                TutorialItem("4", "上传图片", "点击输入框旁的附件/相机按钮，选择图片后发送。支持视觉模型识图。")
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                TutorialItem("5", "中转站配置", "绝大多数厂商都支持 OpenAI 兼容格式（如 DeepSeek、通义千问、智谱等），第三方中转平台（如 One API、New API、OpenRouter 等）同样兼容。通常只需填写平台提供的 API 地址、密钥和模型名称，选择 OpenAI 兼容格式即可接入使用。")
            }
        }

        // 在线指南
        val ctx = LocalContext.current
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("在线指南", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                Text(
                    "更详细的图文教程、视频教程、中转站推荐以及常见问题更新，请查看在线指南。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            ctx.openUrl("https://qcnpe82ha2n0.aiforce.cloud/app/app_4k44ynudr772t")
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("查看详细教程 ↗", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // FAQ
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("常见问题", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                FaqItem("Q: 为什么图片生成失败？", "A: 请确认模型名称正确（建议 gpt-image-2.0），且平台支持图片生成 API。部分中转站需单独开通图片权限。")
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                FaqItem("Q: 为什么文字模型不能识图？", "A: 只有视觉模型（如 GPT-4V、Claude Vision）支持图片识别。普通文字模型发送图片时会被拦截提示。")
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                FaqItem("Q: 数据存在哪里？", "A: 所有数据加密存储在手机本地，不上传任何服务器。清除应用数据或卸载会丢失记录。")
            }
        }
    }
}

@Composable
private fun TutorialItem(num: String, title: String, desc: String) {
    Row(Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(num, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.padding(start = 12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FaqItem(q: String, a: String) {
    Column {
        Text(q, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(a, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
