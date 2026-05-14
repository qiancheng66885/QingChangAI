package com.aiaggregator.app.base.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 时间格式化工具。
 */
object TimeUtil {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private val shortFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    private val timeOnlyFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    /** 获取当前 UTC 时间的 ISO 8601 字符串 */
    fun nowUtcIso(): String = isoFormat.format(Date())

    /** 时间戳（毫秒）转显示格式 "yyyy-MM-dd HH:mm" */
    fun formatDisplay(timestampMs: Long): String = displayFormat.format(Date(timestampMs))

    /** 时间戳转短格式 "MM-dd HH:mm" */
    fun formatShort(timestampMs: Long): String = shortFormat.format(Date(timestampMs))

    /** 时间戳转时间 "HH:mm" */
    fun formatTimeOnly(timestampMs: Long): String = timeOnlyFormat.format(Date(timestampMs))

    /** 判断两个时间戳是否是同一天 */
    fun isSameDay(ts1: Long, ts2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = ts1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = ts2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    /** 生成时间标签：今天"HH:mm"，昨天"昨天 HH:mm"，今年内"MM-dd HH:mm"，更早"yyyy-MM-dd" */
    fun formatChatTime(timestampMs: Long): String {
        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance()
        val todayStart = cal.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayStart = todayStart - 24 * 60 * 60 * 1000L

        return when {
            timestampMs >= todayStart -> formatTimeOnly(timestampMs)  // "14:30"
            timestampMs >= yesterdayStart -> "昨天 ${formatTimeOnly(timestampMs)}"  // "昨天 14:30"
            else -> {
                val isThisYear = cal.apply { timeInMillis = timestampMs }.get(java.util.Calendar.YEAR) ==
                        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                if (isThisYear) formatShort(timestampMs)  // "05-09 14:30"
                else formatDisplay(timestampMs)  // "2026-05-09 14:30"
            }
        }
    }
}
