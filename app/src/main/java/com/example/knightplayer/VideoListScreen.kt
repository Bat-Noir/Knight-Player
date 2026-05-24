package com.example.knightplayer

import android.annotation.SuppressLint
import androidx.compose.foundation.Image // 🔥 ADDED
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.* // 🔥 ADDED (Handles remember, mutableStateOf, LaunchedEffect)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap // 🔥 ADDED
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.knightplayer.core.models.MediaEntry
import com.example.knightplayer.components.KnightGlassCard

@Composable
fun VideoListScreen(
    videos: List<MediaEntry>,
    onVideoClick: (MediaEntry, Int, List<MediaEntry>) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = videos,
            key = { _, video -> video.id }
        ) { index, video ->
            VideoPosterCard(
                video = video,
                onClick = { onVideoClick(video, index, videos) }
            )
        }
    }
}

@Composable
fun VideoPosterCard(
    video: MediaEntry,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    // 🔥 State will now resolve because of the runtime import
    var thumbnailBitmap by remember(video.uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    // 🔥 Effect will now resolve
    LaunchedEffect(video.uri) {
        thumbnailBitmap = com.example.knightplayer.core.data.ThumbnailCache.getThumbnail(context, video.uri.toString())
    }

    KnightGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // 🖼 16:9 THUMBNAIL AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // 🔥 Image will now resolve
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap!!.asImageBitmap(), // 🔥 asImageBitmap will now resolve
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                }

                // ⏱️ Duration Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatDuration(video.duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            // 📝 TITLE TEXT
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(duration: Long): String {
    val totalSeconds = duration / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
