package com.marconi.kipi.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.marconi.kipi.R

actual fun getFontFamily(name: String): FontFamily = when(name) {
    "Domine" -> FontFamily(Font(R.font.domine))
    "Inter" -> FontFamily(Font(R.font.inter))
    "Playfair Display" -> FontFamily(Font(R.font.playfair_display))
    "Roboto" -> FontFamily(Font(R.font.roboto))
    else -> FontFamily.Default
}
