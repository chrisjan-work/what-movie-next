package com.lairofpixies.whatmovienext.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.ui.theme.WhatMovieNextTheme

object MovieListTags {
    const val TAG_MOVIE_LIST = "MovieList"
}

@Composable
fun MovieList(
    movieList: List<Movie>,
    onMovieClicked: (Movie) -> Unit,
) {
    Column(
        modifier = Modifier.testTag(MovieListTags.TAG_MOVIE_LIST),
    ) {
        for (movie in movieList) {
            MovieListItem(movie) { onMovieClicked(movie) }
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie,
    onItemClicked: () -> Unit = {},
) {
    Text(
        text = movie.title,
        modifier = Modifier.clickable { onItemClicked() },
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
