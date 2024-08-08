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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.util.printableRuntime
import com.lairofpixies.whatmovienext.util.printableYear
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.views.components.ThumbnailImage
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.ListMode

@Composable
fun MovieListScreen(listViewModel: MovieListViewModel) {
    Scaffold(
        bottomBar = {
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
                    ),
            )
        },
    ) { innerPadding ->
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Composable
fun MovieList(
    filteredMovies: List<Movie.ForList>,
    onMovieClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag(UiTags.Screens.MOVIE_LIST),
    ) {
        items(filteredMovies) { movie ->
            MovieListItem(movie) { onMovieClicked(movie.appData.movieId) }
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie.ForList,
    onItemClicked: () -> Unit = {},
) {
    val bgColor =
        if (movie.appData.watchState == WatchState.WATCHED) {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.background
        }
    Row(
        modifier =
            Modifier
                .clickable(onClick = onItemClicked)
                .padding(2.dp)
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .background(bgColor)
                .border(
                    border =
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        ),
                    shape = RoundedCornerShape(8.dp),
                ).padding(6.dp),
    ) {
        val maxWidth = if (movie.appData.watchState == WatchState.WATCHED) 260.dp else 320.dp
        ThumbnailImage(
            thumbnailUrl = movie.searchData.thumbnailUrl,
            modifier =
                Modifier
                    .align(Alignment.CenterVertically),
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier =
                Modifier
                    .heightIn(min = 120.dp)
                    .widthIn(max = maxWidth),
        ) {
            Text(
                text = movie.searchData.title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
            )
            YearAndRuntimeDisplay(
                movie.searchData.year,
                movie.detailData.runtimeMinutes,
            )
            RatingsDisplay(
                movie.detailData.mcRating,
                movie.detailData.rtRating,
                modifier = Modifier.alpha(0.8f),
            )
            Spacer(modifier = Modifier.weight(1f))
            if (movie.searchData.genres.isNotEmpty()) {
                Text(
                    text = movie.searchData.genres.joinToString(" / "),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }
            if (movie.detailData.directorNames.isNotEmpty()) {
                val names = movie.detailData.directorNames.joinToString(", ")
                val intro = stringResource(R.string.directed_by_short)
                Text(
                    text = "$intro: $names",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
        if (movie.appData.watchState == WatchState.WATCHED) {
            Spacer(modifier = Modifier.size(8.dp))
            Spacer(modifier = Modifier.weight(1f))
            SeenDisplay(
                movie.appData.watchState,
            )
        }
    }
}

@Composable
fun YearAndRuntimeDisplay(
    year: Int?,
    runtimeMinutes: Int,
    modifier: Modifier = Modifier,
) {
    val dot = stringResource(id = R.string.middle_dot)
    Text(
        text = printableYear(year, pos = "  $dot  ") + printableRuntime(runtimeMinutes),
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}

@Composable
fun SeenDisplay(
    watchState: WatchState,
    modifier: Modifier = Modifier,
) {
    if (watchState == WatchState.WATCHED) {
        val seenIcon = Icons.Outlined.RemoveRedEye
        Icon(
            imageVector = seenIcon,
            contentDescription = "",
            modifier =
                modifier
                    .padding(2.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    .padding(2.dp),
            tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        )
    } else {
        Spacer(
            modifier
                .padding(2.dp)
                .size(24.dp),
        )
    }
}

@Composable
fun RatingsDisplay(
    mcRating: Rating?,
    rtRating: Rating?,
    modifier: Modifier = Modifier,
) {
    Row {
        if (mcRating != null) {
            MetacriticIcon(modifier = modifier)
            RatingDisplay(
                mcRating.displayValue,
                modifier = modifier,
            )
        }
        if (rtRating != null) {
            RottenTomatoesIcon(modifier = modifier)
            RatingDisplay(
                rtRating.displayValue,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun MetacriticIcon(modifier: Modifier = Modifier) {
    Image(
        painterResource(R.drawable.metacritic),
        contentDescription = "",
        modifier =
            modifier
                .padding(2.dp)
                .size(16.dp),
    )
}

@Composable
fun RottenTomatoesIcon(modifier: Modifier = Modifier) {
    Image(
        painterResource(R.drawable.rotten_tomatoes),
        contentDescription = "",
        modifier =
            modifier
                .padding(2.dp)
                .size(16.dp),
        colorFilter = ColorFilter.tint(Color.Red),
    )
}

@Composable
fun RatingDisplay(
    rating: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = rating,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(2.dp),
        color = MaterialTheme.colorScheme.onBackground,
    )
}

fun bottomItemsForMovieList(
    listMode: ListMode,
    isArchiveVisitable: Boolean,
    onListModeChanged: (ListMode) -> Unit,
    onCreateNewMovie: () -> Unit,
    onOpenArchive: () -> Unit,
): List<CustomBarItem> {
    val filterItem =
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

    return listOfNotNull(
        filterItem,
        createItem,
        archiveItem,
    )
}
