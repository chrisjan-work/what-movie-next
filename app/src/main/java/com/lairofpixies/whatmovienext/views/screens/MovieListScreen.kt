package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.ui.theme.WhatMovieNextTheme
import com.lairofpixies.whatmovienext.viewmodel.ListMode

object MovieListTags {
    const val TAG_MODE_BUTTON = "ListModeButton"
    const val TAG_MOVIE_LIST = "MovieList"
}

@Composable
fun MovieList(
    listMode: ListMode,
    movies: List<Movie>,
    onListModeChanged: (ListMode) -> Unit,
    onMovieClicked: (Movie) -> Unit,
) {
    val filteredMovies =
        when (listMode) {
            ListMode.ALL -> movies
            ListMode.WATCHED -> movies.filter { it.watchState == WatchState.WATCHED }
            ListMode.PENDING -> movies.filter { it.watchState == WatchState.PENDING }
        }

    Column(
        modifier = Modifier.testTag(MovieListTags.TAG_MOVIE_LIST),
    ) {
        ListModeButton(listMode, onListModeChanged)
        for (movie in filteredMovies) {
            MovieListItem(movie) { onMovieClicked(movie) }
        }
    }
}

@Composable
fun ListModeButton(
    listMode: ListMode,
    onListModeChanged: (ListMode) -> Unit,
) {
    Button(
        modifier = Modifier.testTag(MovieListTags.TAG_MODE_BUTTON),
        onClick = {
            onListModeChanged(listMode.next())
        },
    ) {
        Text(text = listMode.name)
    }
}

@Preview
@Composable
fun ListModeButtonPreview() {
    val listMode = remember { mutableStateOf(ListMode.ALL) }
    WhatMovieNextTheme {
        ListModeButton(listMode.value) { listMode.value = it }
    }
}

@Composable
fun MovieListItem(
    movie: Movie,
    onItemClicked: () -> Unit = {},
) {
    val backgroundColor =
        when (movie.watchState) {
            WatchState.PENDING -> Color.White
            WatchState.WATCHED -> Color.LightGray
        }
    val foregroundColor =
        when (movie.watchState) {
            WatchState.PENDING -> Color.DarkGray
            WatchState.WATCHED -> Color.Black
        }

    Text(
        text = movie.title,
        modifier =
            Modifier
                .background(backgroundColor)
                .clickable { onItemClicked() },
        color = foregroundColor,
    )
}

@Preview
@Composable
fun MovieListItemPreview() {
    WhatMovieNextTheme {
        MovieListItem(
            Movie(
                id = 1,
                title = "The Silence of the Lambs",
                watchState = WatchState.PENDING,
            ),
        )
    }
}
