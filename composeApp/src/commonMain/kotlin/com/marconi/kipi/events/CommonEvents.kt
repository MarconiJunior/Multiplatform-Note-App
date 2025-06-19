package com.marconi.kipi.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CommonEvents {
    sealed class Event {
        data class SaveNote(val saveNote: () -> Unit) : Event()
    }

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    suspend fun emitEvent(event: Event) {
        _events.emit(event)
    }
}
