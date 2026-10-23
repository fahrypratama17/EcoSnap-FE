package com.example.ecosnap.ui.theme

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
        R.font.PlusJakartaSans_ExtraLight,
        FontWeight.ExtraLight
    ),
    Font(
        R.font.PlusJakartaSans_ExtraLightItalic,
        FontWeight.ExtraLight,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_Light,
        FontWeight.Light
    ),
    Font(
        R.font.PlusJakartaSans_LightItalic,
        FontWeight.Light,
        FontStyle.Italic,
    ),
    Font(
        R.font.PlusJakartaSans_Regular,
        FontWeight.Normal
    ),
    Font(
        R.font.PlusJakartaSans_Medium,
        FontWeight.Medium
    ),
    Font(
        R.font.PlusJakartaSans_MediumItalic,
        FontWeight.Medium,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_SemiBold,
        FontWeight.SemiBold
    ),
    Font(
        R.font.PlusJakartaSans_SemiBoldItalic,
        FontWeight.SemiBold,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_Bold,
        FontWeight.Bold
    ),
    Font(
        R.font.PlusJakartaSans_BoldItalic,
        FontWeight.Bold,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_ExtraBold,
        FontWeight.ExtraBold
    ),
    Font(
        R.font.PlusJakartaSans_ExtraBoldItalic,
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