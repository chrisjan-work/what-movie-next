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
package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.isMissing
import com.lairofpixies.whatmovienext.viewmodels.ArchiveViewModel
import com.lairofpixies.whatmovienext.views.components.DebugTitle
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes

@Composable
fun ArchiveScreen(archiveViewModel: ArchiveViewModel) {
    val archivedMovies: AsyncMovie = archiveViewModel.archivedMovies.collectAsState().value
    val selection = archiveViewModel.selection.collectAsState().value

    if (archivedMovies.isMissing()) {
        archiveViewModel.onLeaveAction()
    }

    Scaffold(
        modifier = Modifier.testTag(UiTags.Screens.ARCHIVE),
        bottomBar = {
            CustomBottomBar(
                items =
                    bottomItemsForArchive(
                        selection = selection,
                        onNavigateToMovieList = { archiveViewModel.onNavigateTo(Routes.AllMoviesView) },
                        onRestoreSelectedMovies = { archiveViewModel.restoreSelectedMovies() },
                        onDeleteSelectedMovies = { archiveViewModel.deleteSelectedMovies() },
                    ),
            )
        },
    ) { innerPadding ->
        Archive(
            archivedMovies = archivedMovies.toList(),
            selection = selection,
            append = { archiveViewModel.select(it) },
            remove = { archiveViewModel.deselect(it) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Composable
fun Archive(
    archivedMovies: List<Movie.ForList>,
    selection: Set<Movie.ForList>,
    append: (Movie.ForList) -> Unit,
    remove: (Movie.ForList) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        DebugTitle("Archive")
        LazyColumn {
            items(archivedMovies) { movie ->
                ArchivedMovieListItem(
                    movie = movie,
                    isSelected = movie in selection,
                ) { isSelected ->
                    if (isSelected) {
                        append(movie)
                    } else {
                        remove(movie)
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivedMovieListItem(
    movie: Movie.ForList,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
) {
    val selectedColor =
        if (isSelected) {
            Color.DarkGray
        } else {
            Color.Black
        }

    val negativeSelectedColor =
        if (isSelected) {
            Color.Gray
        } else {
            Color.White
        }

    Text(
        modifier =
            Modifier
                .background(negativeSelectedColor)
                .clickable {
                    onSelectionChanged(!isSelected)
                },
        color = selectedColor,
        text = movie.searchData.title,
    )
}

fun bottomItemsForArchive(
    selection: Set<Movie.ForList>,
    onNavigateToMovieList: () -> Unit,
    onRestoreSelectedMovies: () -> Unit,
    onDeleteSelectedMovies: () -> Unit,
): List<CustomBarItem> {
    val actionList =
        mutableListOf(
            CustomBarItem(ButtonSpec.MoviesShortcut, onNavigateToMovieList),
        )
    if (selection.isNotEmpty()) {
        actionList.addAll(
            listOf(
                CustomBarItem(ButtonSpec.RestoreAction, onRestoreSelectedMovies),
                CustomBarItem(ButtonSpec.DeleteAction, onDeleteSelectedMovies),
            ),
        )
    }
    return actionList
}
