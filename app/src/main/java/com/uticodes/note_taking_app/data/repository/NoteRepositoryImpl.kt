package com.uticodes.note_taking_app.data.repository

import com.uticodes.note_taking_app.data.local.dao.NoteDao
import com.uticodes.note_taking_app.domain.model.NoteModel
import com.uticodes.note_taking_app.util.Mapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val mapper: Mapper
) : NoteRepository {

    override fun getNotes(): Flow<List<NoteModel>> {
        return noteDao.getAllNoteEntities().map { entities ->
            entities.map(mapper::mapNoteEntityToNote)
        }
    }

    override suspend fun getNoteById(id: Int): NoteModel? {
        return noteDao.getNoteEntityById(id)?.let(mapper::mapNoteEntityToNote)
    }

    override suspend fun addNote(note: NoteModel) {
        noteDao.insertNoteEntity(mapper.mapNoteToNoteEntity(note))
    }

    override suspend fun updateNote(note: NoteModel) {
        noteDao.updateNoteEntity(mapper.mapNoteToNoteEntity(note))
    }

    override suspend fun deleteNote(note: NoteModel) {
        noteDao.deleteNoteEntity(mapper.mapNoteToNoteEntity(note))
    }

    override suspend fun deleteAllNotes() {
        noteDao.deleteAllNoteEntities()
    }
}