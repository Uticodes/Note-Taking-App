package com.uticodes.note_taking_app.presentation.nav

sealed class Screen(val route: String) {
    data object NoteListScreen : Screen("note_list_screen")
    data object NoteDetailScreen : Screen("note_detail_screen/{noteId}") {
        fun createRoute(noteId: Int) = "note_detail_screen/$noteId"
    }
}