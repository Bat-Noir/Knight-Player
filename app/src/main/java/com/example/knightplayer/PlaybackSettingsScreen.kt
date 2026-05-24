package com.example.knightplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.knightplayer.components.KnightGlassCard
import com.example.knightplayer.ui.theme.ThemePreferences

@Composable
fun PlaybackSettingsScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val seekEnabled by ThemePreferences.seekEnabledFlow.collectAsState()
    val skipDuration by ThemePreferences.doubleTapDurationFlow.collectAsState()
    val pinchZoom by ThemePreferences.pinchToZoomFlow.collectAsState()
    val forceSoftware by ThemePreferences.forceSoftwareDecoderFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // 1. GESTURES CARD
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("GESTURES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(start = 8.dp))
            KnightGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Seek Toggle
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Double-Tap Seek", color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = seekEnabled, onCheckedChange = { ThemePreferences.setSeekEnabled(context, it) })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seek Duration (GREY OUT LOGIC)
                    val textColor = if (seekEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    val valueColor = if (seekEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Skip Duration", color = textColor)
                        Text("${skipDuration / 1000}s", color = valueColor, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = skipDuration.toFloat(),
                        onValueChange = { ThemePreferences.setDoubleTapDuration(context, it.toLong()) },
                        valueRange = 5000f..30000f,
                        steps = 4,
                        enabled = seekEnabled // 🔥 Automatically disables and greys out the slider!
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                    // Pinch to Zoom Toggle
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Pinch to Zoom & Pan", color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = pinchZoom, onCheckedChange = { ThemePreferences.setPinchToZoom(context, it) })
                    }
                }
            }
        }

        // 2. ENGINE CARD
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("HARDWARE ENGINE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(start = 8.dp))
            KnightGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Force Software Decoding", color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Fixes corrupted/glitchy videos by bypassing device hardware. (May drain battery faster)",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, end = 16.dp)
                            )
                        }
                        Switch(checked = forceSoftware, onCheckedChange = { ThemePreferences.setForceSoftwareDecoder(context, it) })
                    }
                }
            }
        }

        // 3. Hold to Fast Forward
        val holdEnabled by ThemePreferences.holdToFFEnabledFlow.collectAsState()
        val holdSpeed by ThemePreferences.holdFFSpeedFlow.collectAsState()

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("HOLD TO FAST FORWARD", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            KnightGlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Hold to Fast Forward", color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = holdEnabled, onCheckedChange = { ThemePreferences.setHoldToFFEnabled(context, it) })
                    }

                    // Slider with Grey-out logic
                    val speedTextColor = if(holdEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FF Speed: ${String.format("%.1f", holdSpeed)}x", color = speedTextColor)
                    }
                    Slider(
                        value = holdSpeed,
                        onValueChange = { ThemePreferences.setHoldFFSpeed(context, it) },
                        valueRange = 1.5f..4.0f,
                        enabled = holdEnabled // GREYS OUT AUTOMATICALLY
                    )
                }
            }
        }
    }
}
