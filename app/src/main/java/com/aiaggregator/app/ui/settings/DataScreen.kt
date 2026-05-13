package com.aiaggregator.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiaggregator.app.data.local.InMemoryStore
import java.util.Calendar

private data class TimeRange(val label: String, val timestamp: Long)

@Composable
fun DataScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val now = System.currentTimeMillis()
    val cal = remember { Calendar.getInstance() }

    fun daysAgo(days: Int): Long {
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.timeInMillis
    }

    val ranges = remember {
        listOf(
            TimeRange("今天", daysAgo(0)),
            TimeRange("近 7 天", daysAgo(7)),
            TimeRange("近 30 天", daysAgo(30)),
            TimeRange("近 6 个月", { cal.timeInMillis = now; cal.add(Calendar.MONTH, -6); cal.timeInMillis }()),
            TimeRange("近 1 年", { cal.timeInMillis = now; cal.add(Calendar.YEAR, -1); cal.timeInMillis }()),
            TimeRange("全部", 0L)
        )
    }
    var selected by remember { mutableStateOf<TimeRange?>(null) }
    var showConfirm by remember { mutableStateOf(false) }
    var showCacheConfirm by remember { mutableStateOf(false) }
    val count = if (selected != null && selected!!.timestamp > 0L) {
        remember(selected) { InMemoryStore.countNewerThan(selected!!.timestamp) }
    } else if (selected?.timestamp == 0L) {
        remember { InMemoryStore.countNewerThan(0L) }
    } else null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Cache ──
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Storage, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("缓存数据", style = MaterialTheme.typography.titleSmall)
                        Text("清除应用临时文件和图片缓存", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(
                    onClick = { showCacheConfirm = true },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("清除缓存") }
            }
        }

        // ── History ──
        ElevatedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DeleteForever, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("聊天记录", style = MaterialTheme.typography.titleSmall)
                        Text("选择时间范围删除历史对话", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Time range chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ranges.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { r ->
                                FilterChip(
                                    selected = selected == r,
                                    onClick = { selected = r; showConfirm = true },
                                    label = { Text(r.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCacheConfirm) {
            AlertDialog(
                onDismissRequest = { showCacheConfirm = false },
                icon = { Icon(Icons.Filled.Storage, null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("确认清除缓存") },
                text = { Text("将清除应用临时文件和图片缓存。") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    ctx.cacheDir?.deleteRecursively()
                                    ctx.externalCacheDir?.deleteRecursively()
                                }
                                Toast.makeText(ctx, "缓存已清除", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "清除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                showCacheConfirm = false
                            }
                        }
                    }) { Text("确认清除") }
                },
                dismissButton = {
                    TextButton(onClick = { showCacheConfirm = false }) { Text("取消") }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Confirmation dialog
        if (showConfirm && selected != null) {
            AlertDialog(
                onDismissRequest = { showConfirm = false; selected = null },
                icon = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("确认删除") },
                text = {
                    Text(
                        if (selected!!.timestamp > 0L) {
                            val label = selected!!.label
                            if (count != null) "将删除${label}的 $count 条对话，此操作不可撤销。"
                            else "将删除${label}的对话记录，此操作不可撤销。"
                        } else {
                            if (count != null) "将删除全部 $count 条对话，此操作不可撤销。"
                            else "将删除全部历史记录，此操作不可撤销。"
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                val deleted = withContext(Dispatchers.IO) {
                                    if (selected!!.timestamp > 0L) {
                                        InMemoryStore.clearNewerThan(selected!!.timestamp)
                                    } else {
                                        val total = InMemoryStore.countNewerThan(0L)
                                        InMemoryStore.clearAll()
                                        total
                                    }
                                }
                                Toast.makeText(ctx, "已删除 $deleted 条对话", Toast.LENGTH_SHORT).show()
                                showConfirm = false; selected = null
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Text("确认删除", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false; selected = null }) { Text("取消") }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
