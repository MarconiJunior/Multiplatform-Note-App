package com.marconi.kipi.presentation.add_edit_note.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun FormattingToolbar(
    isBold: Boolean,
    isItalic: Boolean,
    isUnderlined: Boolean,
    fontSize: Int,
    textColor: Color,
    textAlign: TextAlign,
    onBoldToggle: () -> Unit,
    onItalicToggle: () -> Unit,
    onUnderlineToggle: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onTextAlignChange: (TextAlign) -> Unit
) {
    var showColorMenu by remember { mutableStateOf(false) }
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showAlignMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormattingButton(Icons.Default.FormatBold, isBold, onBoldToggle)
            FormattingButton(Icons.Default.FormatItalic, isItalic, onItalicToggle)
            FormattingButton(Icons.Default.FormatUnderlined, isUnderlined, onUnderlineToggle)

            // Alinhamento
            Box {
                FormattingButton(
                    icon = when (textAlign) {
                        TextAlign.Start -> Icons.Default.FormatAlignLeft
                        TextAlign.Center -> Icons.Default.FormatAlignCenter
                        TextAlign.End -> Icons.Default.FormatAlignRight
                        else -> Icons.Default.FormatAlignLeft
                    },
                    isSelected = showAlignMenu
                ) { showAlignMenu = !showAlignMenu }

                DropdownMenu(
                    expanded = showAlignMenu,
                    onDismissRequest = { showAlignMenu = false },
                    offset = DpOffset(x = 0.dp, y = -150.dp)
                ) {
                    listOf(TextAlign.Start, TextAlign.Center, TextAlign.End).forEach { align ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (align) {
                                        TextAlign.Start -> "Left"
                                        TextAlign.Center -> "Center"
                                        TextAlign.End -> "Right"
                                        else -> "Left"
                                    }
                                )
                            },
                            onClick = {
                                onTextAlignChange(align)
                                showAlignMenu = false
                            }
                        )
                    }
                }
            }

            // Tamanho da fonte
            Box {
                FormattingButton(Icons.Default.FormatSize, isSelected = showFontSizeMenu) { showFontSizeMenu = !showFontSizeMenu }

                DropdownMenu(
                    expanded = showFontSizeMenu,
                    onDismissRequest = { showFontSizeMenu = false },
                    offset = DpOffset(x = 0.dp, y = -150.dp)
                ) {
                    listOf(12, 14, 16, 18, 20, 24, 28, 32).forEach { size ->
                        DropdownMenuItem(
                            text = { Text("${size}sp") },
                            onClick = {
                                onFontSizeChange(size)
                                showFontSizeMenu = false
                            }
                        )
                    }
                }
            }

            // Cor do texto
            Box {
                FormattingButton(Icons.Default.FormatColorText, isSelected = showColorMenu) { showColorMenu = !showColorMenu }

                DropdownMenu(
                    expanded = showColorMenu,
                    onDismissRequest = { showColorMenu = false },
                    offset = DpOffset(x = 0.dp, y = -150.dp)
                ) {
                    listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan, Color.Gray)
                        .forEach { color ->
                            DropdownMenuItem(
                                text = {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                },
                                onClick = {
                                    onTextColorChange(color)
                                    showColorMenu = false
                                }
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun FormattingButton(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
