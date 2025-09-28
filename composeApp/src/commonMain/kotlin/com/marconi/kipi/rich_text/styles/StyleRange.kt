package com.marconi.kipi.rich_text.styles

import kotlinx.serialization.Serializable

@Serializable
data class StyleRange(
    val start: Int,
    val end: Int,
    val style: Style,
    val color: Int? = null,
)
