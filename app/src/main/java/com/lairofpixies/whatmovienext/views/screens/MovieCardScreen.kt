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

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.isMissing
import com.lairofpixies.whatmovienext.models.data.printableRuntime
import com.lairofpixies.whatmovienext.util.toAnnotatedString
import com.lairofpixies.whatmovienext.viewmodels.MovieCardViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes

@Composable
fun MovieCardScreen(
    movieId: Long?,
    cardViewModel: MovieCardViewModel,
) {
    val context = LocalContext.current
    val partialMovie = movieId?.let { cardViewModel.getMovie(it).collectAsState().value }

    LaunchedEffect(partialMovie) {
        if (partialMovie.isMissing()) {
            Toast
                .makeText(context, context.getString(R.string.movie_not_found), Toast.LENGTH_SHORT)
                .show()
            cardViewModel.onCancelAction()
        }
    }

    if (partialMovie is AsyncMovieInfo.Single) {
        MovieCard(
            movie = partialMovie.movie,
            onHomeAction = { cardViewModel.onNavigateTo(Routes.AllMoviesView) },
            onEditAction = { id -> cardViewModel.onNavigateWithParam(Routes.EditMovieView, id) },
            onUpdateAction = { id, watchState -> cardViewModel.updateMovieWatched(id, watchState) },
        )
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onHomeAction: () -> Unit,
    onEditAction: (Long) -> Unit,
    onUpdateAction: (Long, WatchState) -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(UiTags.Screens.MOVIE_CARD),
        bottomBar = {
            CustomBottomBar(
                items =
                    bottomItemsForMovieCard(
                        movie,
                        onHomeAction = onHomeAction,
                        onEditAction = onEditAction,
                        onUpdateAction = onUpdateAction,
                    ),
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                    ),
        ) {
            val parentHeight = maxHeight

            Column(
                modifier =
                    Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .heightIn(min = parentHeight)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                AsyncImage(
                    model = movie.coverUrl,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .padding(2.dp)
                            .width(720.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .align(Alignment.CenterHorizontally),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = movie.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (movie.originalTitle.isNotBlank() && movie.originalTitle != movie.title) {
                    Text(
                        text = movie.originalTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
                Text(
                    text = movie.year.toString(),
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        movie.printableRuntime(pos = "  •  ") +
                            movie.genres.joinToString(" / "),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (movie.tagline.isNotBlank()) {
                    Text(
                        text = movie.tagline,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = movie.summary,
                    textAlign = TextAlign.Start,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Spacer(modifier = Modifier.height(36.dp))
                Spacer(modifier = Modifier.weight(1f))
                CreditsLink(
                    text = LocalContext.current.getString(R.string.tmdbCredits),
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .alpha(0.4f),
                )
            }
        }
    }
}

@Composable
fun CreditsLink(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val annotatedString = text.toAnnotatedString(colorResource(id = R.color.link))

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.labelSmall,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
            modifier.clickable {
                annotatedString
                    .getStringAnnotations(tag = "URL", start = 0, end = text.length)
                    .firstOrNull()
                    ?.let { annotation ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    }
            },
    )
}

fun bottomItemsForMovieCard(
    movie: Movie,
    onHomeAction: () -> Unit,
    onEditAction: (Long) -> Unit,
    onUpdateAction: (Long, WatchState) -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(ButtonSpec.MoviesShortcut, onHomeAction),
        if (movie.watchState == WatchState.PENDING) {
            CustomBarItem(ButtonSpec.PendingMovieState) {
                onUpdateAction(movie.id, WatchState.WATCHED)
            }
        } else {
            CustomBarItem(ButtonSpec.WatchedMovieState) {
                onUpdateAction(movie.id, WatchState.PENDING)
            }
        },
        CustomBarItem(ButtonSpec.EditShortcut) { onEditAction(movie.id) },
    )
