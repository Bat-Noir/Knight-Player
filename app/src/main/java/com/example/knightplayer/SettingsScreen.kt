package com.example.knightplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.knightplayer.components.KnightGlassCard

// 🛠️ DATA MODELS FOR SETTINGS
data class SettingGroup(
    val title: String,
    val items: List<SettingItem>
)

data class SettingItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SettingsScreen(onNavigate: (String) -> Unit) {

    val settingsData = listOf(
        SettingGroup(
            title = "PLAYER PREFERENCES",
            items = listOf(
                SettingItem("Playback", "Default speed & resume behavior", Icons.Default.PlayArrow) {
                    onNavigate("playback")
                },
                SettingItem("Subtitles", "Engine & local .srt config", Icons.Default.Subtitles) {
                    onNavigate("subtitles")
                },
                SettingItem("Audio", "Pass-through & boost settings", Icons.Default.VolumeUp) {
                    onNavigate("audio")
                }
            )
        ),
        SettingGroup(
            title = "INTERFACE",
            items = listOf(
                SettingItem("Appearance", "Theme, typography & UI scale", Icons.Default.Palette) {
                    onNavigate("appearance")
                },
                SettingItem("About", "Version 1.0 Alpha", Icons.Default.Info) { }
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(settingsData) { group ->
            Column {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.items.forEach { item ->
                        KnightGlassCard(
                            onClick = item.onClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
