package com.uticodes.note_taking_app.util

import com.uticodes.note_taking_app.data.local.entity.NoteEntity
import com.uticodes.note_taking_app.domain.model.NoteModel

class Mapper {
    fun mapNoteEntityToNote(entity: NoteEntity): NoteModel {
        return NoteModel(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            timestamp = entity.timestamp
        )
    }

    fun mapNoteToNoteEntity(note: NoteModel): NoteEntity {
        return NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            timestamp = note.timestamp
        )
    }
}