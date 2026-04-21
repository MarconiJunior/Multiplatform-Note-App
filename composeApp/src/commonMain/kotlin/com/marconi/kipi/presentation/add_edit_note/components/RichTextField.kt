package com.marconi.kipi.presentation.add_edit_note.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.buildAnnotatedString
import com.marconi.kipi.rich_text.styles.StyleRange
import com.marconi.kipi.rich_text.toSpanStyle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RichTextField(
    text: String,
    hint: StringResource?,
    isHintVisible: Boolean = true,
    styles: List<StyleRange> = emptyList(),
    onValueChange: (String, Int) -> Unit,
    textStyle: TextStyle = TextStyle(),
    singleLine: Boolean = false,
    onFocusChange: (FocusState) -> Unit = {},
    onSelectionChange: (TextRange) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    )

    val annotatedText = remember(text, styles) {
        buildAnnotatedString {
            append(text)
            styles.forEach { styleRange ->
                if (styleRange.start < text.length && styleRange.end <= text.length) {
                    addStyle(styleRange.toSpanStyle(), styleRange.start, styleRange.end)
                }
            }
        }
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        BasicTextField(
            value = textFieldValue.copy(annotatedString = annotatedText),
            onValueChange = { newValue ->
                val oldSelection = textFieldValue.selection
                textFieldValue = newValue

                if (newValue.text != text) {
                    onValueChange(newValue.text, newValue.selection.start)
                }

                if (newValue.selection != oldSelection) {
                    onSelectionChange(newValue.selection)
                }
            },
            singleLine = singleLine,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState -> onFocusChange(focusState) },
            decorationBox = { innerTextField ->
                Box {
                    if (text.isEmpty() && isHintVisible && hint != null) {
                        Text(text = stringResource(hint), style = textStyle)
                    }
                    innerTextField()
                }
            }
        )
    }

    LaunchedEffect(text, styles) {
        if (textFieldValue.text != text) {
            val currentSelection = textFieldValue.selection
            val adjustedSelection = when {
                text.length < currentSelection.start -> TextRange(text.length)
                text.length < currentSelection.end -> TextRange(currentSelection.start, text.length)
                else -> currentSelection
            }

            val newAnnotatedText = buildAnnotatedString {
                append(text)
                styles.forEach { styleRange ->
                    if (styleRange.start < text.length && styleRange.end <= text.length) {
                        addStyle(styleRange.toSpanStyle(), styleRange.start, styleRange.end)
                    }
                }
            }

            textFieldValue = TextFieldValue(annotatedString = newAnnotatedText, selection = adjustedSelection)

            if (adjustedSelection != currentSelection) {
                onSelectionChange(adjustedSelection)
            }
        }
    }
}
