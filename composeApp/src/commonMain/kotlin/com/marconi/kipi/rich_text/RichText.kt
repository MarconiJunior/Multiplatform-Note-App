package com.marconi.kipi.rich_text

import com.marconi.kipi.rich_text.styles.StyleRange
import kotlinx.serialization.Serializable

@Serializable
data class RichText(
    val text: String,
    val styles: List<StyleRange>
)
