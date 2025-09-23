package com.uticodes.note_taking_app.domain.model

data class NoteModel(
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long
)