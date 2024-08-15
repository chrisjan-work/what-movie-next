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
package com.lairofpixies.whatmovienext.views.screens.card

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.isMissing
import com.lairofpixies.whatmovienext.viewmodels.MovieCardViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.screens.UiTags

@Composable
fun MovieCardScreen(
    movieId: Long?,
    cardViewModel: MovieCardViewModel,
) {
    val partialMovie = cardViewModel.currentMovie.collectAsState().value

    LaunchedEffect(movieId) {
        cardViewModel.startFetchingMovie(movieId)
    }

    if (partialMovie.isMissing()) {
        val context = LocalContext.current
        Toast
            .makeText(context, context.getString(R.string.movie_not_found), Toast.LENGTH_SHORT)
            .show()
        cardViewModel.onLeaveAction()
    }

    val loadedMovie = partialMovie.singleMovieOrNull<Movie.ForCard>()
    if (loadedMovie != null) {
        val bottomItems =
            bottomItemsForMovieCard(
                loadedMovie,
                onHomeAction = { cardViewModel.onNavigateTo(Routes.AllMoviesView) },
                onArchiveAction = {
                    cardViewModel.archiveCurrentMovie()
                    cardViewModel.onLeaveAction()
                },
                onUpdateAction = { updateMovieId, watchState ->
                    cardViewModel.updateMovieWatched(
                        updateMovieId,
                        watchState,
                    )
                },
            )

        MovieCard(
            movie = loadedMovie,
            bottomItems = bottomItems,
            modifier = Modifier.testTag(UiTags.Screens.MOVIE_CARD),
        )
    } else {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        )
    }
}

fun bottomItemsForMovieCard(
    movie: Movie.ForCard,
    onHomeAction: () -> Unit,
    onArchiveAction: () -> Unit,
    onUpdateAction: (Long, WatchState) -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(ButtonSpec.MoviesShortcut, onHomeAction),
        if (movie.appData.watchState == WatchState.PENDING) {
            CustomBarItem(ButtonSpec.PendingMovieState) {
                onUpdateAction(movie.appData.movieId, WatchState.WATCHED)
            }
        } else {
            CustomBarItem(ButtonSpec.WatchedMovieState) {
                onUpdateAction(movie.appData.movieId, WatchState.PENDING)
            }
        },
        CustomBarItem(ButtonSpec.ArchiveAction, onArchiveAction),
    )
