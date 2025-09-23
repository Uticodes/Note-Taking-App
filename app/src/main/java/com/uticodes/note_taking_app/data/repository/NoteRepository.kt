package com.uticodes.note_taking_app.data.repository

import com.uticodes.note_taking_app.domain.model.NoteModel
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<NoteModel>>
    suspend fun getNoteById(id: Int): NoteModel?
    suspend fun addNote(note: NoteModel)
    suspend fun updateNote(note: NoteModel)
    suspend fun deleteNote(note: NoteModel)
    suspend fun deleteAllNotes()
}