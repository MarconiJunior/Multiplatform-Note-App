package com.marconi.kipi.presentation.add_edit_note.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.marconi.kipi.rich_text.styles.Style
import com.marconi.kipi.rich_text.styles.StyleRange

@Composable
fun RichTextToolbar(
    onStyleToggle: (Style) -> Unit,
    activeStyles: Set<Style>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
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

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun StyleButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.onSurface
        )
    }
}
