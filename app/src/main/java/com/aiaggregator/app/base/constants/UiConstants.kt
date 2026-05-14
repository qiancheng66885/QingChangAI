package com.aiaggregator.app.base.constants

import androidx.compose.ui.unit.dp

/**
 * UI 相关的常量定义。
 */
object UiConstants {

    // === 间距 ===
    val SPACING_XSMALL = 4.dp
    val SPACING_SMALL = 8.dp
    val SPACING_MEDIUM = 12.dp
    val SPACING_LARGE = 16.dp
    val SPACING_XLARGE = 24.dp
    val SPACING_XXLARGE = 32.dp

    // === 圆角 ===
    val CORNER_SMALL = 8.dp
    val CORNER_MEDIUM = 12.dp
    val CORNER_LARGE = 16.dp
    val CORNER_ROUND = 999.dp  // 完全圆角

    // === 尺寸 ===
    val AVATAR_SIZE_SMALL = 32.dp
    val AVATAR_SIZE_MEDIUM = 40.dp
    val AVATAR_SIZE_LARGE = 56.dp
    val ICON_SIZE_SMALL = 18.dp
    val ICON_SIZE_MEDIUM = 24.dp
    val ICON_SIZE_LARGE = 32.dp
    val MIN_TOUCH_TARGET = 48.dp  // Material Design 最小触控区域

    // === 动画 ===
    const val ANIM_DURATION_SHORT = 200
    const val ANIM_DURATION_MEDIUM = 350
    const val ANIM_DURATION_LONG = 500

    // === 布局 ===
    val CHAT_INPUT_MAX_HEIGHT = 150.dp
    val MESSAGE_BUBBLE_MAX_WIDTH_RATIO = 0.75f
    val SIDE_DRAWER_WIDTH_RATIO = 0.85f
}
