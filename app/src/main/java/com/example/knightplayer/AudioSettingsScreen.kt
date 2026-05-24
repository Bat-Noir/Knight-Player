package com.example.knightplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.knightplayer.components.KnightGlassCard
import com.example.knightplayer.ui.theme.ThemePreferences

@Composable
fun AudioSettingsScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Collect States
    val volumeBoost by ThemePreferences.volumeBoostFlow.collectAsState()
    val audioPassthrough by ThemePreferences.audioPassthroughFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // 1. DSP ENGINE CARD
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("DSP EFFECTS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(start = 8.dp))
            KnightGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Volume Boost (200%)", color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Uses software amplification to push volume past system limits. May cause distortion.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, end = 16.dp)
                            )
                        }
                        Switch(checked = volumeBoost, onCheckedChange = { ThemePreferences.setVolumeBoost(context, it) })
                    }
                }
            }
        }

        // 2. HARDWARE DECODING CARD
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("DECODER PROTOCOLS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(start = 8.dp))
            KnightGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hardware Pass-through", color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Send raw audio (EAC3, DTS, Dolby) directly to the hardware. Turn off if videos have no sound.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, end = 16.dp)
                            )
                        }
                        Switch(checked = audioPassthrough, onCheckedChange = { ThemePreferences.setAudioPassthrough(context, it) })
                    }
                }
            }
        }
    }
}
