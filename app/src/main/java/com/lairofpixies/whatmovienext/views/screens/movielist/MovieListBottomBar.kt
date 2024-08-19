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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode

@Composable
fun MovieListBottomBar(
    listViewModel: MovieListViewModel,
    modifier: Modifier = Modifier,
) {
    CustomBottomBar(
        items =
            bottomItemsForMovieList(
                listFilters =
                    listViewModel.currentPreset
                        .collectAsState()
                        .value.listFilters,
                isArchiveVisitable = listViewModel.hasArchivedMovies.collectAsState().value,
                isRouleteActive = listViewModel.canSpinRoulette(),
                onListFiltersChanged = { listViewModel.setListFilters(it) },
                onCreateNewMovie = {
                    listViewModel.onNavigateTo(Routes.CreateMovieView)
                },
                onSortingClicked = {
                    listViewModel.onOpenSortingMenu()
                },
                onRouletteClicked = {
                    listViewModel.onNavigateToRandomMovie()
                },
                onOpenArchive = {
                    listViewModel.onNavigateTo(Routes.ArchiveView)
                },
            ),
        modifier = modifier,
    )
}

fun bottomItemsForMovieList(
    listFilters: ListFilters,
    isArchiveVisitable: Boolean,
    isRouleteActive: Boolean,
    onListFiltersChanged: (ListFilters) -> Unit,
    onCreateNewMovie: () -> Unit,
    onSortingClicked: () -> Unit,
    onRouletteClicked: () -> Unit,
    onOpenArchive: () -> Unit,
): List<CustomBarItem> {
    val seenFilter =
        CustomBarItem(
            when (listFilters.listMode) {
                ListMode.ALL -> ButtonSpec.AllMoviesFilter
                ListMode.PENDING -> ButtonSpec.PendingFilter
                ListMode.WATCHED -> ButtonSpec.WatchedFilter
            },
            tag = UiTags.Buttons.LIST_MODE,
        ) {
            onListFiltersChanged(
                listFilters.let {
                    it.copy(listMode = it.listMode.next())
                },
            )
        }

    val createItem = CustomBarItem(ButtonSpec.CreateMovieShortcut, onCreateNewMovie)

    val rouletteItem =
        CustomBarItem(
            ButtonSpec.RouletteAction,
            tag = UiTags.Buttons.ROULETTE,
            enabled = isRouleteActive,
            onClick = onRouletteClicked,
        )

    val sortingItem =
        CustomBarItem(
            ButtonSpec.SortingMenu,
            tag = UiTags.Buttons.SORT_MENU,
            onClick = onSortingClicked,
        )

    val archiveItem =
        if (isArchiveVisitable) {
            CustomBarItem(ButtonSpec.ArchiveShortcut, onOpenArchive)
        } else {
            null
        }

    return listOfNotNull(
        sortingItem,
        seenFilter,
        archiveItem,
        rouletteItem,
        createItem,
    )
}
