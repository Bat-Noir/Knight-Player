package com.example.knightplayer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.knightplayer.R

// 🔤 ALL 4 FONT FAMILIES
val NDot = FontFamily(Font(R.font.ndot57_aligned, FontWeight.Normal))
val NType = FontFamily(
    Font(R.font.ntype82_regular, FontWeight.Normal),
    Font(R.font.ntype82_bold, FontWeight.Bold)
)
val SFPro = FontFamily(
    Font(R.font.sf_pro_regular, FontWeight.Normal),
    Font(R.font.sf_pro_bold, FontWeight.Bold)
)
val Poppins = FontFamily(Font(R.font.poppins_semibold, FontWeight.SemiBold))

// 📐 SCALED-DOWN DESIGN SYSTEM
val KnightTypography = Typography(
    // 1. TOP BAR HEADINGS
    displayLarge = TextStyle(
        fontFamily = NType,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp, // 👈 Increased to 32.sp for proper emphasis
        letterSpacing = 0.5.sp
    ),
    // 2. CARD TITLES / SECTION HEADERS (NType Bold)
    titleLarge = TextStyle(
        fontFamily = NType,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 0.5.sp
    ),
    // 3. STANDARD BODY TEXT (SF Pro Bold)
    bodyLarge = TextStyle(
        fontFamily = SFPro,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    ),
    // 4. BADGES / READOUTS (NDot)
    labelMedium = TextStyle(
        fontFamily = NDot,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
)

