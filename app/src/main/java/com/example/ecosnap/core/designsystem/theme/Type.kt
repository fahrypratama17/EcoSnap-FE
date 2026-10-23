package com.example.ecosnap.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ecosnap.R

val PlusJakartaSans = FontFamily(
    Font(
        R.font.plus_jakarta_sans_extra_light,
        FontWeight.ExtraLight
    ),
    Font(
        R.font.plus_jakarta_sans_extra_light_italic,
        FontWeight.ExtraLight,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_light,
        FontWeight.Light
    ),
    Font(
        R.font.plus_jakarta_sans_light_italic,
        FontWeight.Light,
        FontStyle.Italic,
    ),
    Font(
        R.font.plus_jakarta_sans_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_medium,
        FontWeight.Medium
    ),
    Font(
        R.font.plus_jakarta_sans_medium_italic,
        FontWeight.Medium,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_semi_bold,
        FontWeight.SemiBold
    ),
    Font(
        R.font.plus_jakarta_sans_semi_bold_italic,
        FontWeight.SemiBold,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_bold,
        FontWeight.Bold
    ),
    Font(
        R.font.plus_jakarta_sans_bold_italic,
        FontWeight.Bold,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_extra_bold,
        FontWeight.ExtraBold
    ),
    Font(
        R.font.plus_jakarta_sans_extra_bold_italic,
        FontWeight.ExtraBold,
        FontStyle.Italic
    )
)

// Set of Material typography styles to start with
val ecoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    ),
)