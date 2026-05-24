package com.example.knightplayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The core building block for the Knight OS UI.
 * Creates a translucent, bordered glass card for settings, library items, etc.
 */
@Composable
fun KnightGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // 🔥 DYNAMIC GLASS: Adapts to Light/Dark mode automatically
    val glassColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    var baseModifier = modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(glassColor)
        // 🔥 The Blade Runner detail: A hyper-thin border to catch the edge
        .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))

    if (onClick != null) {
        baseModifier = baseModifier.clickable { onClick() }
    }

    Box(
        modifier = baseModifier,
        content = content
    )
}
