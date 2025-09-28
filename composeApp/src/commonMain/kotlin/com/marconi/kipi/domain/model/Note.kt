package com.marconi.kipi.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marconi.kipi.ui.theme.BabyBlue
import com.marconi.kipi.ui.theme.RedPink
import com.marconi.kipi.ui.theme.Violet

@Entity
data class Note(
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
    val textColor: Int,
    val fontSize: Float,
    val fontStyle: String?,
    val richTextStyles: String? = null, // JSON serializado dos StyleRange
    @PrimaryKey val id: Int? = null
) {
    companion object {
        val noteColors = listOf(Violet, BabyBlue, RedPink)
    }
}

class InvalidNoteException(message: String): Exception(message)

