package com.uticodes.note_taking_app.presentation.note_view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uticodes.note_taking_app.domain.model.NoteModel
import com.uticodes.note_taking_app.domain.usecase.NotesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val notesUseCase: NotesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteModel>>(emptyList())
    val notes: StateFlow<List<NoteModel>> = _notes.asStateFlow()

    private val _effects = Channel<NotesEffect>(Channel.BUFFERED)
    val effects: Flow<NotesEffect> = _effects.receiveAsFlow()

    init {
        getNotes()
    }

    private fun getNotes() {
        notesUseCase.getNotes().onEach { notes ->
            _notes.value = notes
        }.launchIn(viewModelScope)

        savedStateHandle.getStateFlow("result", "")
            .filter { it.isNotBlank() }
            .onEach {
                _effects.send(NotesEffect.ShowMessage(it))
                savedStateHandle["result"] = ""
            }
            .launchIn(viewModelScope)
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            notesUseCase.deleteAll()
            _effects.send(NotesEffect.ShowMessage("All notes deleted"))
        }
    }
}

sealed interface NotesEffect { data class ShowMessage(val text: String): NotesEffect }
