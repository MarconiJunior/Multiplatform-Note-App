package com.marconi.note.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marconi.note.ui.theme.BabyBlue
import com.marconi.note.ui.theme.RedPink
import com.marconi.note.ui.theme.Violet

@Entity
data class Note(
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
    val textColor: Int,
    val fontSize: Float,
    @PrimaryKey val id: Int? = null
) {
    companion object {
        val noteColors = listOf(Violet, BabyBlue, RedPink)
    }
}

class InvalidNoteException(message: String): Exception(message)

