package com.marconi.kipi.rich_text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
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
        Style.H1 -> SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Style.H2 -> SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Style.H3 -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Style.COLOR -> SpanStyle()
    }
    return if (color != null) baseStyle.copy(color = Color(color)) else baseStyle
}

fun List<StyleRange>.optimizeStyles(): List<StyleRange> {
    if (isEmpty()) return this

    val sorted = sortedWith(compareBy<StyleRange> { it.start }.thenBy { it.end })
    val optimized = mutableListOf<StyleRange>()

    for (current in sorted) {
        val overlapping = optimized.filter { existing ->
            existing.style == current.style &&
                    existing.color == current.color &&
                    existing.start < current.end &&
                    existing.end > current.start
        }

        if (overlapping.isEmpty()) {
            optimized.add(current)
        } else {
            optimized.removeAll(overlapping)
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

// Bug fix: usa flatMap para poder retornar 0, 1 ou 2 elementos por range
fun List<StyleRange>.removeStylesInRange(
    start: Int,
    end: Int,
    style: Style? = null
): List<StyleRange> {
    return flatMap { existing ->
        when {
            style != null && existing.style != style -> listOf(existing)
            existing.start >= start && existing.end <= end -> emptyList()
            existing.end <= start || existing.start >= end -> listOf(existing)
            else -> {
                val parts = mutableListOf<StyleRange>()
                if (existing.start < start) parts.add(existing.copy(end = start))
                if (existing.end > end) parts.add(existing.copy(start = end))
                parts
            }
        }
    }
}

fun List<StyleRange>.hasStyleInRange(start: Int, end: Int, style: Style): Boolean {
    return any { it.style == style && it.start <= start && it.end >= end }
}

// Desloca ranges após inserção de texto
fun List<StyleRange>.adjustForInsertion(insertStart: Int, insertLength: Int): List<StyleRange> {
    return map { styleRange ->
        when {
            styleRange.end <= insertStart -> styleRange
            styleRange.start >= insertStart -> styleRange.copy(
                start = styleRange.start + insertLength,
                end = styleRange.end + insertLength
            )
            else -> styleRange.copy(end = styleRange.end + insertLength)
        }
    }
}

// Ajusta ranges após deleção de texto
fun List<StyleRange>.adjustForDeletion(deleteStart: Int, deleteEnd: Int): List<StyleRange> {
    val deleteLength = deleteEnd - deleteStart
    return flatMap { styleRange ->
        when {
            styleRange.end <= deleteStart -> listOf(styleRange)
            styleRange.start >= deleteEnd -> listOf(
                styleRange.copy(
                    start = styleRange.start - deleteLength,
                    end = styleRange.end - deleteLength
                )
            )
            styleRange.start >= deleteStart && styleRange.end <= deleteEnd -> emptyList()
            else -> {
                val newStart = minOf(styleRange.start, deleteStart)
                val newEnd = if (styleRange.end > deleteEnd) styleRange.end - deleteLength else deleteStart
                if (newStart >= newEnd) emptyList()
                else listOf(styleRange.copy(start = newStart, end = newEnd))
            }
        }
    }
}

fun List<StyleRange>.stylesAtPosition(position: Int): Set<Style> {
    return filter { it.start < position && it.end >= position }
        .map { it.style }
        .toSet()
}

fun List<StyleRange>.colorAtPosition(position: Int): Int? {
    return filter { it.style == Style.COLOR && it.start < position && it.end >= position }
        .firstOrNull()?.color
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
