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
package com.lairofpixies.whatmovienext.views.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.viewmodels.SharedMovieViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.screens.card.MovieCard

@Composable
fun SharedMovieScreen(
    tmdbId: Long?,
    sharedMovieViewModel: SharedMovieViewModel,
) {
    LaunchedEffect(tmdbId) {
        if (tmdbId == null) {
            sharedMovieViewModel.failAndLeave()
        } else {
            sharedMovieViewModel.fetchFromRemote(tmdbId)
        }
    }

    val foundMovie = sharedMovieViewModel.foundMovie.collectAsState().value
    foundMovie.singleMovieOrNull<Movie.ForCard>()?.let { movie ->
        MovieCard(
            movie = movie,
            bottomItems =
                bottomItemsForSharedView(
                    onCancelAction = { sharedMovieViewModel.onLeaveAction() },
                    onSaveAction = { sharedMovieViewModel.onSaveAction() },
                ),
            modifier = Modifier.testTag(UiTags.Screens.SHARED_VIEW),
        )
    }
}

fun bottomItemsForSharedView(
    onCancelAction: () -> Unit,
    onSaveAction: () -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(ButtonSpec.CancelAction, onCancelAction),
        CustomBarItem(ButtonSpec.SaveAction, onSaveAction),
    )
