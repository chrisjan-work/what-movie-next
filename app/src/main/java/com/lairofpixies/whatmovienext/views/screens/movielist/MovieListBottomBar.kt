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
import com.lairofpixies.whatmovienext.views.state.ListMode

@Composable
fun MovieListBottomBar(
    listViewModel: MovieListViewModel,
    modifier: Modifier = Modifier,
) {
    CustomBottomBar(
        items =
            bottomItemsForMovieList(
                listMode = listViewModel.listMode.collectAsState().value,
                isArchiveVisitable = listViewModel.hasArchivedMovies.collectAsState().value,
                onListModeChanged = { listViewModel.setListMode(it) },
                onCreateNewMovie = {
                    listViewModel.onNavigateTo(Routes.CreateMovieView)
                },
                onOpenArchive = {
                    listViewModel.onNavigateTo(Routes.ArchiveView)
                },
                onSortingClicked = {
                    listViewModel.onOpenSortingMenu()
                },
            ),
        modifier = modifier,
    )
}

fun bottomItemsForMovieList(
    listMode: ListMode,
    isArchiveVisitable: Boolean,
    onListModeChanged: (ListMode) -> Unit,
    onCreateNewMovie: () -> Unit,
    onOpenArchive: () -> Unit,
    onSortingClicked: () -> Unit,
): List<CustomBarItem> {
    val seenFilter =
        CustomBarItem(
            when (listMode) {
                ListMode.ALL -> ButtonSpec.AllMoviesFilter
                ListMode.PENDING -> ButtonSpec.PendingFilter
                ListMode.WATCHED -> ButtonSpec.WatchedFilter
            },
            tag = UiTags.Buttons.LIST_MODE,
        ) {
            onListModeChanged(listMode.next())
        }

    val createItem = CustomBarItem(ButtonSpec.CreateMovieShortcut, onCreateNewMovie)

    val archiveItem =
        if (isArchiveVisitable) {
            CustomBarItem(ButtonSpec.ArchiveShortcut, onOpenArchive)
        } else {
            null
        }

    val sortingItem =
        CustomBarItem(
            ButtonSpec.SortingMenu,
            tag = UiTags.Buttons.SORT_MENU,
            onClick = onSortingClicked,
        )

    return listOfNotNull(
        sortingItem,
        seenFilter,
        archiveItem,
        createItem,
    )
}
