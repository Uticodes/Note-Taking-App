package com.uticodes.note_taking_app.domain.usecase

import com.uticodes.note_taking_app.data.repository.NoteRepository
import com.uticodes.note_taking_app.domain.model.NoteModel
import kotlinx.coroutines.flow.Flow

class NotesUseCase(private val repo: NoteRepository) {
    fun getNotes(): Flow<List<NoteModel>> = repo.getNotes()

    suspend fun getNoteById(id: Int): NoteModel? = repo.getNoteById(id)

    suspend fun addNote(note: NoteModel) = repo.addNote(note)

    suspend fun updateNote(note: NoteModel) = repo.updateNote(note)

    suspend fun deleteNote(note: NoteModel) = repo.deleteNote(note)

    suspend fun deleteAll() = repo.deleteAllNotes()
}
