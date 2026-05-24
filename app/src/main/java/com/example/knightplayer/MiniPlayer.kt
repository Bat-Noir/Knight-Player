package com.example.knightplayer

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.knightplayer.core.data.ThumbnailCache
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    history: WatchHistory,
    onClick: () -> Unit,
    onExpand: () -> Unit,
    onDismiss: () -> Unit = {} // 🔥 THE FIX: Added onDismiss parameter
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(true) }

    // 🔥 THE PRO FIX: This lets the exit animation finish before destroying the composable!
    LaunchedEffect(visible) {
        if (!visible) {
            delay(250) // Wait for slideOutVertically to finish
            onDismiss() // Now tell MainActivity it's safe to permanently hide
        }
    }

    val progress = if (history.duration > 0) {
        history.position.toFloat() / history.duration
    } else 0f

    var thumbnail by remember(history.uri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(history.uri) {
        thumbnail = ThumbnailCache.getThumbnail(context, history.uri)
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }), // Slide up from bottom
        exit = slideOutVertically(targetOffsetY = { it })   // Slide down to bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(72.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 20) visible = false // swipe down hides it
                        if (dragAmount < -20) onExpand()     // swipe up opens video
                    }
                }
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface) // Fallback placeholder
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f).padding(top = 4.dp)
                ) {
                    Text(
                        text = history.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${history.position / 60000} min",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth(0.85f).height(3.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
