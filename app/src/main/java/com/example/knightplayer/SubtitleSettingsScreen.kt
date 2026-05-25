package com.example.knightplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.knightplayer.components.KnightGlassCard // Make sure this path is correct for your project
import com.example.knightplayer.ui.theme.SubtitleColor
import com.example.knightplayer.ui.theme.SubtitleFont
import com.example.knightplayer.ui.theme.ThemePreferences

@Composable
fun SubtitleSettingsScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Collect all states
    val font by ThemePreferences.subFontFlow.collectAsState()
    val size by ThemePreferences.subSizeFlow.collectAsState()
    val textColor by ThemePreferences.subTextColorFlow.collectAsState()
    val bgColor by ThemePreferences.subBgColorFlow.collectAsState()
    val bgOpacity by ThemePreferences.subBgOpacityFlow.collectAsState()
    val borderColor by ThemePreferences.subBorderColorFlow.collectAsState()
    val borderWidth by ThemePreferences.subBorderWidthFlow.collectAsState()
    val posY by ThemePreferences.subPosYFlow.collectAsState()
    val posX by ThemePreferences.subPosXFlow.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // 📺 LIVE PREVIEW WINDOW (Sticky at top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.verticalGradient(listOf(Color.DarkGray, Color.Black)))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(x = posX.dp, y = -posY.dp) // 🔥 THE FIX: Added minus sign to posY.dp
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor.colorValue.copy(alpha = bgOpacity))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // To do true text borders in Compose, we draw the Stroke first, then the Fill over it.
                Box {
                    if (borderWidth > 0f) {
                        Text(
                            text = "This is a live preview.",
                            style = TextStyle(
                                fontFamily = font.getFontFamily(), // 🔥 FIXED HERE
                                fontSize = size.sp,
                                fontWeight = FontWeight.Bold,
                                drawStyle = Stroke(width = borderWidth)
                            ),
                            color = borderColor.colorValue,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "This is a live preview.",
                        style = TextStyle(
                            fontFamily = font.getFontFamily(), // 🔥 FIXED HERE
                            fontSize = size.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textColor.colorValue,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 🎛️ CONTROLS (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 1. TYPOGRAPHY CARD
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("TYPOGRAPHY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                KnightGlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Font Family", color = MaterialTheme.colorScheme.onSurface)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                            items(SubtitleFont.values()) { f ->
                                val isSelected = f == font
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .border(1.dp, if (isSelected) Color.Transparent else Color.Gray, RoundedCornerShape(12.dp))
                                        .clickable { ThemePreferences.setSubFont(context, f) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = f.fontName,
                                        fontFamily = f.getFontFamily(), // 🔥 FIXED HERE
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Text("Text Size: ${size.toInt()}sp", color = MaterialTheme.colorScheme.onSurface)
                        Slider(value = size, onValueChange = { ThemePreferences.setSubSize(context, it) }, valueRange = 12f..48f)
                    }
                }
            }

            // 2. COLORS & BORDER CARD
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("COLORS & OUTLINE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                KnightGlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Text Color", color = MaterialTheme.colorScheme.onSurface)
                        ColorRow(SubtitleColor.values().toList(), textColor) { ThemePreferences.setSubTextColor(context, it) }

                        Spacer(Modifier.height(16.dp))
                        Text("Border Color", color = MaterialTheme.colorScheme.onSurface)
                        ColorRow(SubtitleColor.values().toList(), borderColor) { ThemePreferences.setSubBorderColor(context, it) }

                        Spacer(Modifier.height(8.dp))
                        Text("Border Thickness: ${borderWidth.toInt()}px", color = MaterialTheme.colorScheme.onSurface)
                        Slider(value = borderWidth, onValueChange = { ThemePreferences.setSubBorderWidth(context, it) }, valueRange = 0f..15f)
                    }
                }
            }

            // 3. BACKGROUND CARD
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("BACKGROUND", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                KnightGlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Background Color", color = MaterialTheme.colorScheme.onSurface)
                        ColorRow(SubtitleColor.values().toList(), bgColor) { ThemePreferences.setSubBgColor(context, it) }

                        Spacer(Modifier.height(8.dp))
                        Text("Background Opacity: ${(bgOpacity * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface)
                        Slider(value = bgOpacity, onValueChange = { ThemePreferences.setSubBgOpacity(context, it) }, valueRange = 0f..1f)
                    }
                }
            }

            // 4. POSITION CARD
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("POSITIONING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                KnightGlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Y-Axis (Bottom Padding): ${posY.toInt()}dp", color = MaterialTheme.colorScheme.onSurface)
                        Slider(value = posY, onValueChange = { ThemePreferences.setSubPosY(context, it) }, valueRange = -100f..200f)

                        Spacer(Modifier.height(8.dp))
                        Text("X-Axis (Horizontal Shift): ${posX.toInt()}dp", color = MaterialTheme.colorScheme.onSurface)
                        Slider(value = posX, onValueChange = { ThemePreferences.setSubPosX(context, it) }, valueRange = -200f..200f)
                    }
                }
            }
        }
    }
}

// Reusable color picker row
@Composable
fun ColorRow(colors: List<SubtitleColor>, selected: SubtitleColor, onSelect: (SubtitleColor) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.colorValue)
                    .border(3.dp, if (color == selected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                    .clickable { onSelect(color) }
            )
        }
    }
}
