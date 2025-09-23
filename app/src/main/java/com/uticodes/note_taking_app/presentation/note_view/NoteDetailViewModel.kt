package com.uticodes.note_taking_app.presentation.note_view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uticodes.note_taking_app.domain.model.NoteModel
import com.uticodes.note_taking_app.domain.usecase.NotesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val noteId: Int,
    private val notesUseCase: NotesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _note = MutableStateFlow<NoteModel?>(null)
    val note: StateFlow<NoteModel?> = _note.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    init {
        if (noteId != 0) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: Int) {
        viewModelScope.launch {
            val existing = notesUseCase.getNoteById(noteId)
            _note.value = existing
            if (existing != null) {
                if (_title.value.isEmpty())  _title.value = existing.title
                if (_content.value.isEmpty()) _content.value = existing.content
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
        savedStateHandle[KEY_TITLE] = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
        savedStateHandle[KEY_CONTENT] = newContent
    }

    fun saveNote(onNavigateBack: () -> Unit) {
        viewModelScope.launch {
            val t = _title.value.trim()
            val c = _content.value.trim()
            if (t.isEmpty()) return@launch

            val model = NoteModel(
                id = if (noteId > 0) noteId else 0,
                title = t,
                content = c,
                timestamp = System.currentTimeMillis()
            )

            if (noteId > 0) notesUseCase.updateNote(model) else notesUseCase.addNote(model)
            clearDraft()
            onNavigateBack()
        }
    }

    private fun clearDraft() {
        savedStateHandle[KEY_TITLE] = ""
        savedStateHandle[KEY_CONTENT] = ""
    }

    fun deleteNote(onComplete: () -> Unit) {
        viewModelScope.launch {
            _note.value?.let { noteToDelete ->
                notesUseCase.deleteNote(noteToDelete)
                onComplete()
            }
        }
    }

    companion object {
        private const val KEY_TITLE = "draft_title"
        private const val KEY_CONTENT = "draft_content"

        fun Factory(noteId: Int, notesUseCase: NotesUseCase): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val handle = createSavedStateHandle()
                    NoteDetailViewModel(
                        noteId = noteId,
                        notesUseCase = notesUseCase,
                        savedStateHandle = handle
                    )
                }
            }
    }
}