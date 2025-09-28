package com.marconi.kipi.rich_text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.marconi.kipi.rich_text.styles.Style
import com.marconi.kipi.rich_text.styles.StyleRange
import kotlinx.serialization.json.Json

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

fun serialize(richText: RichText): String {
    return json.encodeToString(RichText.serializer(), richText)
}

fun deserialize(jsonString: String): RichText {
    return json.decodeFromString<RichText>(jsonString)
}

fun RichText.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        styles.forEach { styleRange ->
            val spanStyle = styleRange.toSpanStyle()
            addStyle(spanStyle, styleRange.start, styleRange.end)
        }
    }
}

fun StyleRange.toSpanStyle(): SpanStyle {
    val baseStyle = when (style) {
        Style.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        Style.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        Style.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
        Style.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    }

    return if (color != null) {
        baseStyle.copy(color = Color(color))
    } else {
        baseStyle
    }
}

fun List<StyleRange>.optimizeStyles(): List<StyleRange> {
    if (isEmpty()) return this

    val sorted = sortedWith(compareBy<StyleRange> { it.start }.thenBy { it.end })
    val optimized = mutableListOf<StyleRange>()

    for (current in sorted) {
        val overlapping = optimized.filter { existing ->
            existing.style == current.style &&
                    existing.start < current.end &&
                    existing.end > current.start
        }

        if (overlapping.isEmpty()) {
            optimized.add(current)
        } else {
            // Remove os estilos sobrepostos
            optimized.removeAll(overlapping)

            // Cria um novo estilo que combina todos os ranges
            val minStart = minOf(current.start, overlapping.minOf { it.start })
            val maxEnd = maxOf(current.end, overlapping.maxOf { it.end })

            optimized.add(
                StyleRange(
                    start = minStart,
                    end = maxEnd,
                    style = current.style,
                    color = current.color ?: overlapping.firstOrNull()?.color
                )
            )
        }
    }

    return optimized.sortedBy { it.start }
}

fun List<StyleRange>.removeStylesInRange(
    start: Int,
    end: Int,
    style: Style? = null
): List<StyleRange> {
    return mapNotNull { existing ->
        when {
            style != null && existing.style != style -> existing

            existing.start >= start && existing.end <= end -> null

            existing.end <= start || existing.start >= end -> existing

            else -> {
                val parts = mutableListOf<StyleRange>()

                if (existing.start < start) {
                    parts.add(existing.copy(end = start))
                }

                if (existing.end > end) {
                    parts.add(existing.copy(start = end))
                }

                parts.firstOrNull()
            }
        }
    }
}

fun List<StyleRange>.hasStyleInRange(start: Int, end: Int, style: Style): Boolean {
    return any { existing ->
        existing.style == style &&
                existing.start <= start &&
                existing.end >= end
    }
}

fun serializeStyles(styles: List<StyleRange>): String {
    return json.encodeToString(styles)
}

fun deserializeStyles(jsonString: String?): List<StyleRange> {
    return if (jsonString.isNullOrEmpty()) {
        emptyList()
    } else {
        try {
            json.decodeFromString<List<StyleRange>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}