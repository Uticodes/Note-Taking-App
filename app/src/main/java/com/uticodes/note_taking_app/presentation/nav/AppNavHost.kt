package com.uticodes.note_taking_app.presentation.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.uticodes.note_taking_app.NoteApp
import com.uticodes.note_taking_app.presentation.note_view.NoteDetailScreen
import com.uticodes.note_taking_app.presentation.note_view.NoteDetailViewModel
import com.uticodes.note_taking_app.presentation.note_view.NotesScreen
import com.uticodes.note_taking_app.presentation.note_view.NotesViewModel
import com.uticodes.note_taking_app.util.NotesViewModelFactory

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Screen.NoteListScreen.route,
    application: NoteApp
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.NoteListScreen.route) {
            val viewModel: NotesViewModel = viewModel(
                factory = NotesViewModelFactory.create()
            )
            NotesScreen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetailScreen.createRoute(noteId))
                },
                onAddNoteClick = {
                    navController.navigate(Screen.NoteDetailScreen.createRoute(0))
                }
            )
        }
        composable(
            route = Screen.NoteDetailScreen.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            val viewModel: NoteDetailViewModel = viewModel(
                factory = NoteDetailViewModel.Factory(
                    noteId = noteId,
                    notesUseCase = application.appModule.notesUseCase,
                )
            )
            NoteDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}