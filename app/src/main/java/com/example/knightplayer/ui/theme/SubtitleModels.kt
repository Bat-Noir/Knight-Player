package com.example.knightplayer.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.knightplayer.R

enum class SubtitleColor(val colorName: String, val colorValue: Color) {
    WHITE("White", Color.White),
    YELLOW("Yellow", Color.Yellow),
    BLACK("Black", Color.Black),
    RED("Red", Color(0xFFFF4444)),
    GREEN("Green", Color(0xFF00C853)),
    BLUE("Blue", Color(0xFF33B5E5)),
    CYAN("Cyan", Color.Cyan),
    MAGENTA("Magenta", Color.Magenta)
}

enum class SubtitleFont(val fontName: String, val fontResId: Int) {
    SYSTEM("System Default", -1),
    NDOT("Ndot 57", R.font.ndot57_aligned),
    NTYPE_BOLD("Ntype Bold", R.font.ntype82_bold),
    NTYPE_REGULAR("Ntype Regular", R.font.ntype82_regular),
    POPPINS("Poppins", R.font.poppins_semibold),
    SF_BOLD("SF Pro Bold", R.font.sf_pro_bold),
    SF_REGULAR("SF Pro Regular", R.font.sf_pro_regular);

    // This helper makes it easy to use in your UI
    fun getFontFamily(): FontFamily {
        return if (fontResId == -1) FontFamily.Default
        else FontFamily(Font(fontResId))
    }
}
