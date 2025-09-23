package com.uticodes.note_taking_app.di

import android.content.Context
import com.uticodes.note_taking_app.data.repository.NoteRepository
import com.uticodes.note_taking_app.data.repository.NoteRepositoryImpl
import com.uticodes.note_taking_app.domain.usecase.NotesUseCase
import com.uticodes.note_taking_app.util.Mapper

class AppModule(context: Context) {

    private val databaseModule = DatabaseModule(context)
    private val mapper = Mapper()

    val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(databaseModule.noteDao, mapper)
    }

    val notesUseCase: NotesUseCase by lazy {
        NotesUseCase(noteRepository)
    }

}