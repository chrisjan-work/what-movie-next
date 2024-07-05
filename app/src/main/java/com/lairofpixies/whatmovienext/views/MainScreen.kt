package com.lairofpixies.whatmovienext.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.lairofpixies.whatmovienext.ui.theme.WhatMovieNextTheme
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState = viewModel.uiState.collectAsState().value

    WhatMovieNextTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (uiState.expandedMovie) {
                null ->
                    MovieList(
                        uiState.listMode,
                        uiState.movieList,
                        onListModeChanged = { viewModel.setListMode(it) },
                        onMovieClicked = { viewModel.expandMovieAction(it) },
                    )

                else ->
                    MovieDetails(
                        movie = uiState.expandedMovie,
                        onCloseAction = { viewModel.closeMovieAction() },
                        onUpdateAction = { viewModel.updateMovieWatched(it.id, it.watchState) },
                        onArchiveAction = { viewModel.archiveMovieAction(it.id) },
                    )
            }
        }
    }
}
