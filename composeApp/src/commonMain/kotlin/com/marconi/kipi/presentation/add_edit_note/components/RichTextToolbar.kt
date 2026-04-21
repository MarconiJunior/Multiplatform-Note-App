package com.marconi.kipi.presentation.add_edit_note.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconi.kipi.rich_text.styles.Style

@Composable
fun RichTextToolbar(
    onStyleToggle: (Style) -> Unit,
    activeStyles: Set<Style>,
    activeColor: Int? = null,
    onColorPick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StyleButton(
                icon = Icons.Default.FormatBold,
                isActive = Style.BOLD in activeStyles,
                onClick = { onStyleToggle(Style.BOLD) }
            )
            StyleButton(
                icon = Icons.Default.FormatItalic,
                isActive = Style.ITALIC in activeStyles,
                onClick = { onStyleToggle(Style.ITALIC) }
            )
            StyleButton(
                icon = Icons.Default.FormatUnderlined,
                isActive = Style.UNDERLINE in activeStyles,
                onClick = { onStyleToggle(Style.UNDERLINE) }
            )
            StyleButton(
                icon = Icons.Default.FormatStrikethrough,
                isActive = Style.STRIKETHROUGH in activeStyles,
                onClick = { onStyleToggle(Style.STRIKETHROUGH) }
            )

            ToolbarDivider()

            HeadingButton(label = "H1", isActive = Style.H1 in activeStyles) { onStyleToggle(Style.H1) }
            HeadingButton(label = "H2", isActive = Style.H2 in activeStyles) { onStyleToggle(Style.H2) }
            HeadingButton(label = "H3", isActive = Style.H3 in activeStyles) { onStyleToggle(Style.H3) }

            ToolbarDivider()

            IconButton(onClick = onColorPick) {
                Icon(
                    imageVector = Icons.Default.FormatColorText,
                    contentDescription = null,
                    tint = if (activeColor != null) Color(activeColor)
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StyleButton(icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HeadingButton(label: String, isActive: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    )
}
