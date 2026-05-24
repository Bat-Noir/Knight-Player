package com.example.knightplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.knightplayer.components.KnightGlassCard
import com.example.knightplayer.ui.theme.AccentColorOption
import com.example.knightplayer.ui.theme.ThemeModeOption
import com.example.knightplayer.ui.theme.ThemePreferences

@Composable
fun AppearanceScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentThemeMode by ThemePreferences.themeModeFlow.collectAsState()
    val currentAccent by ThemePreferences.accentColorFlow.collectAsState()
    val seekbarThickness by ThemePreferences.seekbarThicknessFlow.collectAsState()
    val iconScale by ThemePreferences.iconScaleFlow.collectAsState()
    val topIconOrder by ThemePreferences.topIconOrderFlow.collectAsState()

    // 🔥 STATE FOR TAP-TO-SWAP
    var selectedForSwap by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // 1. THEME MODE
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("THEME MODE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ThemeModeOption.entries.forEach { mode ->
                    ThemeModeCard(Modifier.weight(1f), mode.name, currentThemeMode == mode) { ThemePreferences.setThemeMode(context, mode) }
                }
            }
        }

        // 2. ACCENT COLOR
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ACCENT COLOR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(AccentColorOption.entries) { option ->
                    ColorPickerCircle(option, currentAccent == option) { ThemePreferences.setAccentColor(context, option) }
                }
            }
        }

        // 3. LIVE PLAYER PREVIEW
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PLAYER PREVIEW (Tap icons to swap)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))

                val dynamicIconSize = (28 * iconScale.coerceIn(0.7f, 1.2f)).dp

                // 🔥 TOP ROW
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize))
                    Spacer(Modifier.width(8.dp))
                    Text("Invincible S01E04", color = Color.White, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)

                    // 🔥 TAP-TO-SWAP UTILITY ICONS
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        topIconOrder.forEach { iconName ->
                            val isSelected = selectedForSwap == iconName
                            val iconVector = when(iconName) {
                                "SPEED" -> Icons.Default.Speed
                                "AUDIO" -> Icons.Default.MusicNote
                                "SUBTITLES" -> Icons.Default.Subtitles
                                "PLAYLIST" -> Icons.Default.PlaylistPlay
                                else -> Icons.Default.Help
                            }
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(dynamicIconSize).clickable {
                                    if (selectedForSwap == null) {
                                        selectedForSwap = iconName // Select first icon
                                    } else if (selectedForSwap == iconName) {
                                        selectedForSwap = null // Deselect
                                    } else {
                                        // Swap logic!
                                        val newList = topIconOrder.toMutableList()
                                        val idx1 = newList.indexOf(selectedForSwap)
                                        val idx2 = newList.indexOf(iconName)
                                        newList[idx1] = iconName
                                        newList[idx2] = selectedForSwap!!
                                        ThemePreferences.setTopIconOrder(context, newList)
                                        selectedForSwap = null
                                    }
                                }
                            )
                        }
                    }
                }

                // 🔥 CENTER ROW (Play/Pause/Skip)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size((36 * iconScale.coerceIn(0.7f, 1.2f)).dp))
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size((56 * iconScale.coerceIn(0.7f, 1.2f)).dp))
                    Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size((36 * iconScale.coerceIn(0.7f, 1.2f)).dp))
                }

                // 🔥 BOTTOM ROW (Seekbar & Tools)
                Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Slider(
                        value = 0.4f, onValueChange = {},
                        modifier = Modifier.fillMaxWidth().height(seekbarThickness.dp).graphicsLayer { renderEffect = null },
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = Color.White.copy(0.3f))
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("05:21", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("45:00", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Default.Crop, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize))
                            Icon(Icons.Default.ScreenRotation, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize))
                        }
                    }
                }
            }
        }

        // 4. CONTROLS
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("CONTROLS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            KnightGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ControlSlider("Seekbar Position", seekbarThickness, { ThemePreferences.setSeekbarThickness(context, it) }, 4f..20f, "${seekbarThickness.toInt()} dp")
                    Spacer(Modifier.height(16.dp))
                    ControlSlider("Icon Scale", iconScale, { ThemePreferences.setIconScale(context, it.coerceIn(0.7f, 1.2f)) }, 0.7f..1.2f, "${(iconScale * 100).toInt()}%")
                }
            }
        }
    }
}

@Composable
fun ControlSlider(label: String, value: Float, onValueChange: (Float) -> Unit, range: ClosedFloatingPointRange<Float>, textValue: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onSurface)
            Text(textValue, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range, colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun ThemeModeCard(modifier: Modifier = Modifier, label: String, isSelected: Boolean, onClick: () -> Unit) {
    KnightGlassCard(modifier = modifier.height(60.dp), cornerRadius = 16.dp, onClick = onClick) {
        Box(modifier = Modifier.fillMaxSize().background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent).border(width = if (isSelected) 1.5.dp else 0.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, shape = RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun ColorPickerCircle(colorOption: AccentColorOption, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(colorOption.colorValue).clickable { onClick() }, contentAlignment = Alignment.Center) {
        if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}
