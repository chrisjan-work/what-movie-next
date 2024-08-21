/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.lairofpixies.whatmovienext.views.screens.movielist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.views.components.CustomScaffold
import com.lairofpixies.whatmovienext.views.navigation.Routes

@Composable
fun MovieListScreen(listViewModel: MovieListViewModel) {
    val isMenuShown =
        listViewModel.bottomMenuState
            .collectAsState()
            .value.isOpen
    BackHandler(isMenuShown) {
        listViewModel.closeBottomMenu()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        CustomScaffold(
            bottomBar = {
                MovieListBottomBar(
                    listViewModel = listViewModel,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            },
            topBar = { trigger ->
                MovieListTopBar(
                    trigger = trigger,
                    isArchiveVisitable = listViewModel.hasArchivedMovies.collectAsState().value,
                    onOpenArchive = {
                        listViewModel.onNavigateTo(Routes.ArchiveView)
                    },
                )
            },
        ) { _, onScrollEvent ->
            MovieList(
                filteredMovies =
                    listViewModel.listedMovies
                        .collectAsState()
                        .value
                        .toList(),
                onMovieClicked = { movieId ->
                    listViewModel.onNavigateWithParam(
                        Routes.SingleMovieView,
                        movieId,
                    )
                },
                onScrollEvent = onScrollEvent,
                modifier =
                    Modifier
                        .fillMaxSize(),
            )
        }

        val currentPreset = listViewModel.currentPreset.collectAsState().value
        MovieListBottomSheet(
            bottomMenuState = listViewModel.bottomMenuState,
            selectMenu = { option ->
                listViewModel.onOpenBottomMenu(option)
            },
            sortingSetup = currentPreset.sortingSetup,
            updateSortingSetup = { listViewModel.updateSortingSetup(it) },
            closeBottomMenu = { listViewModel.closeBottomMenu() },
            listFilters = currentPreset.listFilters,
            onListFiltersChanged = { listViewModel.setListFilters(it) },
        )
    }
}
