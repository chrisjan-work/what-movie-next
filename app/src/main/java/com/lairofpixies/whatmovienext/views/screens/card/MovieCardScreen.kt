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
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.isMissing
import com.lairofpixies.whatmovienext.viewmodels.MovieCardViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.screens.UiTags
import kotlin.math.roundToInt

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
                movie = loadedMovie,
                isRouletteAvailable = cardViewModel.canSpinRoulette(),
                onRouletteAction = { cardViewModel.onNavigateToRandomMovie(loadedMovie.appData.movieId) },
                onReplaceDatesAction = { updateMovieId, watchDates ->
                    cardViewModel.updateMovieWatchDates(
                        updateMovieId,
                        watchDates,
                    )
                },
            )

        MovieCard(
            movie = loadedMovie,
            bottomItems = bottomItems,
            topBar = { trigger ->
                MovieCardTopBar(
                    trigger = trigger,
                    onArchiveAction = {
                        cardViewModel.archiveCurrentMovie()
                        cardViewModel.onLeaveAction()
                    },
                )
            },
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

@Composable
fun MovieCardTopBar(
    trigger: State<Boolean>,
    onArchiveAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topBarHeightPx = with(LocalDensity.current) { 56.dp.toPx() }
    val topBarOffset =
        animateIntOffsetAsState(
            targetValue =
                if (trigger.value) {
                    IntOffset.Zero
                } else {
                    IntOffset(x = 0, y = -topBarHeightPx.roundToInt())
                },
            label = "topbar offset animation",
        )

    Box(
        modifier =
            modifier
                .offset { topBarOffset.value }
                .background(MaterialTheme.colorScheme.background)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f))
                .fillMaxWidth(),
    ) {
        Icon(
            ButtonSpec.ArchiveAction.icon,
            contentDescription = stringResource(ButtonSpec.ArchiveAction.labelRes),
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
                    .alpha(0.8f)
                    .clickable { onArchiveAction() }
                    .testTag(UiTags.Buttons.ARCHIVE_ACTION),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}

fun bottomItemsForMovieCard(
    movie: Movie.ForCard,
    isRouletteAvailable: Boolean,
    onRouletteAction: () -> Unit,
    onReplaceDatesAction: (Long, List<Long>) -> Unit,
): List<CustomBarItem> {
    val rouletteItem =
        if (isRouletteAvailable) {
            CustomBarItem(ButtonSpec.RouletteAction, onRouletteAction)
        } else {
            null
        }

    // TODO: enter dates here
    val seenItem =
        if (movie.appData.watchDates.isEmpty()) {
            CustomBarItem(ButtonSpec.PendingMovieState) {
                onReplaceDatesAction(movie.appData.movieId, listOf(System.currentTimeMillis()))
            }
        } else {
            CustomBarItem(ButtonSpec.WatchedMovieState) {
                onReplaceDatesAction(movie.appData.movieId, emptyList())
            }
        }

    return listOfNotNull(
        seenItem,
        rouletteItem,
    )
}
