package com.dresso.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.dresso.app.R

val Cormorant = FontFamily(
    Font(R.font.cormorant_light, FontWeight.Light),
    Font(R.font.cormorant_regular, FontWeight.Normal),
    Font(R.font.cormorant_medium, FontWeight.Medium),
    Font(R.font.cormorant_semibold, FontWeight.SemiBold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

private val Trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

val DressoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Light,
        fontSize = 58.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1).sp,
        lineHeightStyle = Trim,
    ),
    displayMedium = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Light,
        fontSize = 46.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.6).sp,
        lineHeightStyle = Trim,
    ),
    displaySmall = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Light,
        fontSize = 38.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.4).sp,
        lineHeightStyle = Trim,
    ),
    headlineLarge = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Normal,
        fontSize = 27.sp,
        lineHeight = 31.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.05.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.05.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.7.sp,
    ),
)

val WordmarkStyle = TextStyle(
    fontFamily = Cormorant,
    fontWeight = FontWeight.Medium,
    fontSize = 22.sp,
    lineHeight = 22.sp,
    letterSpacing = 5.sp,
)

val EyebrowStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 9.sp,
    lineHeight = 13.sp,
    letterSpacing = 2.sp,
)
