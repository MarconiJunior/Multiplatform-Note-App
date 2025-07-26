package com.marconi.kipi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

val AppTypography = Typography()

expect fun getFontFamily(name: String): FontFamily
