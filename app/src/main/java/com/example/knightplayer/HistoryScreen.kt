package com.example.knightplayer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.knightplayer.core.data.HistoryManager
import com.example.knightplayer.core.data.ThumbnailCache

@Composable
fun HistoryScreen(
    onVideoClick: (WatchHistory) -> Unit
) {
    val context = LocalContext.current

    // 🔥 THE FIX: Listen to the reactive flow! No manual refreshing needed.
    val history by HistoryManager.historyFlow.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 110.dp
        )
    ) {
        items(history) { item ->
            val progress = if (item.duration > 0) item.position.toFloat() / item.duration else 0f
            var thumbnail by remember(item.uri) { mutableStateOf<Bitmap?>(null) }

            LaunchedEffect(item.uri) {
                thumbnail = ThumbnailCache.getThumbnail(context, item.uri)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onVideoClick(item) },
                verticalAlignment = Alignment.CenterVertically
            ) {

                ////////////////////////////////////////////////////
                // THUMBNAIL
                ////////////////////////////////////////////////////
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant) // 🔥 DYNAMIC PLACEHOLDER
                    )
                }

                Spacer(Modifier.width(14.dp))

                ////////////////////////////////////////////////////
                // VIDEO INFO
                ////////////////////////////////////////////////////
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        color = MaterialTheme.colorScheme.onSurface, // 🔥 DYNAMIC MAIN TEXT
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            color = MaterialTheme.colorScheme.primary, // 🔥 DYNAMIC ACCENT
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), // 🔥 DYNAMIC TRACK
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // 🔥 DYNAMIC ICON
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    HistoryManager.removeHistory(context, item.videoId)
                                    // No manual "history = ..." needed here anymore!
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatTime(item.position) + " / " + formatTime(item.duration),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // 🔥 DYNAMIC DIMMED TEXT
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
