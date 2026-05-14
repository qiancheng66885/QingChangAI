package com.aiaggregator.app.ui.config

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.model.ApiFormatType
import com.aiaggregator.app.data.model.ModelCategory
import com.aiaggregator.app.data.model.ModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class Vendor(
    val id: String,
    val label: String,
    val baseUrl: String,
    val formatType: ApiFormatType,
    val description: String,
    val baseUrlPlaceholder: String,
    val baseUrlHelp: String,
    val isCustom: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigEditScreen(
    vm: ConfigViewModel,
    modelId: String?,
    onCancel: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vendors = remember {
        listOf(
            Vendor(
                id = "openai",
                label = "OpenAI 兼容格式",
                baseUrl = "https://api.openai.com/v1",
                formatType = ApiFormatType.OPENAI_COMPATIBLE,
                description = "官方地址，OpenAI 兼容接口。",
                baseUrlPlaceholder = "https://api.openai.com/v1",
                baseUrlHelp = "已自动填入 OpenAI 官方地址；如果你用的是代理或中转站，请改成实际地址。"
            ),
            Vendor(
                id = "anthropic",
                label = "Anthropic 兼容格式",
                baseUrl = "https://api.anthropic.com",
                formatType = ApiFormatType.ANTHROPIC_COMPATIBLE,
                description = "官方地址，Claude 兼容接口。",
                baseUrlPlaceholder = "https://api.anthropic.com",
                baseUrlHelp = "已自动填入 Anthropic 官方地址；如果你用的是中转站，请改成实际地址。"
            ),
            Vendor(
                id = "deepseek",
                label = "DeepSeek 兼容格式",
                baseUrl = "https://api.deepseek.com/v1",
                formatType = ApiFormatType.OPENAI_COMPATIBLE,
                description = "官方地址，OpenAI 兼容格式。",
                baseUrlPlaceholder = "https://api.deepseek.com/v1",
                baseUrlHelp = "已自动填入 DeepSeek 官方地址；如果你用的是中转站，请改成实际地址。"
            ),
            Vendor(
                id = "custom_openai",
                label = "自定义 OpenAI 兼容",
                baseUrl = "",
                formatType = ApiFormatType.OPENAI_COMPATIBLE,
                description = "中转站或自建 OpenAI 兼容服务。",
                baseUrlPlaceholder = "https://你的平台或中转站域名/v1",
                baseUrlHelp = "请填写你实际使用的平台、中转站或模型服务地址，例如 https://你的域名/v1。自定义项不会自动带默认地址。",
                isCustom = true
            ),
            Vendor(
                id = "custom_anthropic",
                label = "自定义 Anthropic 兼容",
                baseUrl = "",
                formatType = ApiFormatType.ANTHROPIC_COMPATIBLE,
                description = "中转站或自建 Claude 兼容服务。",
                baseUrlPlaceholder = "https://你的平台或中转站域名",
                baseUrlHelp = "请填写你实际使用的 Anthropic 兼容平台或中转站地址。自定义项不会自动带默认地址。",
                isCustom = true
            )
        )
    }
    val configState by vm.state.collectAsState()
    val editingModel = remember(modelId, configState.models) { modelId?.let { id -> configState.models.find { it.id == id } } }
    val editingPlatform = remember(editingModel?.platformId, configState.platforms) { editingModel?.let { configState.platforms.find { platform -> platform.id == it.platformId } } }
    val initialVendor = remember(editingPlatform) {
        val platform = editingPlatform
        when {
            platform == null -> vendors.first()
            platform.baseUrl == "https://api.openai.com/v1" -> vendors.first { it.id == "openai" }
            platform.baseUrl == "https://api.anthropic.com" -> vendors.first { it.id == "anthropic" }
            platform.baseUrl == "https://api.deepseek.com/v1" -> vendors.first { it.id == "deepseek" }
            platform.formatType == ApiFormatType.ANTHROPIC_COMPATIBLE -> vendors.first { it.id == "custom_anthropic" }
            else -> vendors.first { it.id == "custom_openai" }
        }
    }

    var vendorExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedVendor by remember { mutableStateOf(initialVendor) }
    var apiKey by remember { mutableStateOf(editingPlatform?.apiKey ?: "") }
    var baseUrl by remember { mutableStateOf(editingPlatform?.baseUrl ?: initialVendor.baseUrl) }
    var modelName by remember { mutableStateOf(editingModel?.modelName ?: "") }
    var displayName by remember { mutableStateOf(editingModel?.displayName ?: "") }
    var category by remember { mutableStateOf(editingModel?.category ?: ModelCategory.CHAT) }
    var endpointChat by remember { mutableStateOf(editingPlatform?.chatEndpoint ?: "") }
    var endpointImageGen by remember { mutableStateOf(editingPlatform?.imageGenEndpoint ?: "") }
    var endpointImageEdit by remember { mutableStateOf(editingPlatform?.imageEditEndpoint ?: "") }
    var showAdvanced by remember { mutableStateOf(endpointChat.isNotBlank() || endpointImageGen.isNotBlank() || endpointImageEdit.isNotBlank()) }
    var saving by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (modelId == null) "添加模型" else "编辑模型", style = MaterialTheme.typography.titleLarge)
                Text(
                    "配置会本地保存。支持中转站、OpenAI 兼容接口和 Anthropic 兼容接口。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Text(
                        "聊天/多模态模型通常可直接使用 OpenAI 或 Anthropic 兼容格式；图片生成优先建议通过中转平台或 OpenAI 兼容接口接入。",
                        Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                ExposedDropdownMenuBox(vendorExpanded, { vendorExpanded = it }) {
                    OutlinedTextField(
                        selectedVendor.label,
                        {},
                        readOnly = true,
                        label = { Text("选择厂商") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(vendorExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = vendorExpanded,
                        onDismissRequest = { vendorExpanded = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        vendors.forEachIndexed { index, vendor ->
                            if (index > 0) {
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 14.dp),
                                    thickness = 0.6.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Column(
                                        Modifier.padding(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            vendor.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (vendor.id == selectedVendor.id) FontWeight.SemiBold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            vendor.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                onClick = {
                                    selectedVendor = vendor
                                    baseUrl = vendor.baseUrl
                                    endpointChat = ""
                                    endpointImageGen = ""
                                    endpointImageEdit = ""
                                    showAdvanced = false
                                    vendorExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    apiKey,
                    { apiKey = it },
                    label = { Text("API 密钥") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("sk-...") }
                )
                OutlinedTextField(
                    baseUrl,
                    { baseUrl = it },
                    label = { Text("API 地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(selectedVendor.baseUrlPlaceholder) }
                )
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedVendor.isCustom) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    }
                ) {
                    Text(
                        selectedVendor.baseUrlHelp,
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedVendor.isCustom) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                if (selectedVendor.id == "custom_openai" || selectedVendor.id == "custom_anthropic") {
                    TextButton(onClick = { showAdvanced = !showAdvanced }) {
                        Text(if (showAdvanced) "收起高级选项" else "展开高级选项")
                    }
                    if (showAdvanced) {
                        val defaultChatEndpoint = if (selectedVendor.formatType == ApiFormatType.ANTHROPIC_COMPATIBLE) "/v1/messages" else "/v1/chat/completions"
                        OutlinedTextField(
                            endpointChat,
                            { endpointChat = it },
                            label = { Text("聊天端点路径") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text(defaultChatEndpoint) }
                        )
                        if (category == ModelCategory.IMAGE) {
                            OutlinedTextField(
                                endpointImageGen,
                                { endpointImageGen = it },
                                label = { Text("图像生成端点") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("/v1/images/generations") }
                            )
                            OutlinedTextField(
                                endpointImageEdit,
                                { endpointImageEdit = it },
                                label = { Text("图像编辑端点") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("/v1/images/edits") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    modelName,
                    { modelName = it },
                    label = { Text("模型名（API 用）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("gpt-4o / claude-sonnet-4-6") }
                )
                OutlinedTextField(
                    displayName,
                    { displayName = it },
                    label = { Text("显示名称（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("如：GPT-4o 工作") }
                )

                ExposedDropdownMenuBox(categoryExpanded, { categoryExpanded = it }) {
                    OutlinedTextField(
                        category.label,
                        {},
                        readOnly = true,
                        label = { Text("模型类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        listOf(ModelCategory.CHAT, ModelCategory.IMAGE, ModelCategory.MULTIMODAL).forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 14.dp),
                                    thickness = 0.6.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        item.label,
                                        Modifier.padding(vertical = 6.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    category = item
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                val categoryHint = when (category) {
                    ModelCategory.CHAT -> "文本对话模型，不支持识别图片。"
                    ModelCategory.MULTIMODAL -> "多模态模型，可发送图片让模型识别和理解。"
                    ModelCategory.IMAGE -> "图片生成模型，通过文字描述生成图片。若厂商接口格式特殊，建议通过中转平台接入。"
                    else -> ""
                }
                if (categoryHint.isNotBlank()) {
                    Surface(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
                    ) {
                        Text(
                            categoryHint,
                            Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row {
                    TextButton(
                        onClick = {
                            if (saving) Toast.makeText(context, "正在保存，请稍候", Toast.LENGTH_SHORT).show() else onCancel()
                        }
                    ) { Text("取消") }
                    Spacer(Modifier.width(12.dp))
                    val canSave = apiKey.isNotBlank() && baseUrl.isNotBlank() && modelName.isNotBlank()
                    FilledTonalButton(
                        onClick = {
                            if (saving) {
                                Toast.makeText(context, "正在保存，请稍候", Toast.LENGTH_SHORT).show()
                                return@FilledTonalButton
                            }
                            if (!canSave) return@FilledTonalButton
                            saving = true
                            val savedModelName = displayName.ifBlank { modelName }
                            val savedPlatformName = selectedVendor.label
                            val platform = ApiConfig(
                                id = editingModel?.platformId ?: java.util.UUID.randomUUID().toString(),
                                platformName = savedPlatformName,
                                baseUrl = baseUrl,
                                apiKey = apiKey,
                                formatType = selectedVendor.formatType,
                                chatEndpoint = endpointChat.ifBlank { null },
                                imageGenEndpoint = endpointImageGen.ifBlank { null },
                                imageEditEndpoint = endpointImageEdit.ifBlank { null }
                            )
                            val model = ModelConfig(
                                id = editingModel?.id ?: java.util.UUID.randomUUID().toString(),
                                platformId = platform.id,
                                displayName = displayName,
                                modelName = modelName,
                                category = category,
                                isDefault = if (category == ModelCategory.IMAGE) false else editingModel?.isDefault ?: false,
                                showInPicker = editingModel?.showInPicker ?: true
                            )
                            scope.launch {
                                try {
                                    vm.upsertConfig(platform, model)
                                    Toast.makeText(context, "已保存：${savedPlatformName} / ${savedModelName}", Toast.LENGTH_SHORT).show()
                                    onSaved()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "保存失败：${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        enabled = canSave || saving
                    ) { Text(if (saving) "保存中..." else "保存") }
                }
            }
        }
    }
}
