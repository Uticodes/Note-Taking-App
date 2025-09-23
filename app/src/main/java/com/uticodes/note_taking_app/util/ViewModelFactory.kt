package com.uticodes.note_taking_app.util

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uticodes.note_taking_app.NoteApp
import com.uticodes.note_taking_app.domain.usecase.NotesUseCase
import com.uticodes.note_taking_app.presentation.note_view.NotesViewModel

object NotesViewModelFactory {
    fun create(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteApp
            val uc: NotesUseCase = app.appModule.notesUseCase
            NotesViewModel(uc, createSavedStateHandle())
        }
    }
}

