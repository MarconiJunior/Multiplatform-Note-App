package com.marconi.kipi.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.marconi.kipi.R

actual fun getFontFamily(name: FontTypes): FontFamily = when(name) {
    FontTypes.Domine -> FontFamily(Font(R.font.domine))
    FontTypes.Inter -> FontFamily(Font(R.font.inter))
    FontTypes.PlayfairDisplay -> FontFamily(Font(R.font.playfair_display))
    FontTypes.Roboto -> FontFamily(Font(R.font.roboto))
    else -> FontFamily.Default
}
