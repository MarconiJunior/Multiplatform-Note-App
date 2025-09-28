package com.marconi.kipi.presentation.add_edit_note

import androidx.compose.ui.text.TextRange
import com.marconi.kipi.rich_text.styles.StyleRange

data class RichTextUiState(
    val selection: TextRange = TextRange.Zero,
    val styles: List<StyleRange> = emptyList()
)