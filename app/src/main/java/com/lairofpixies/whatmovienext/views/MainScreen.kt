package com.lairofpixies.whatmovienext.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.ui.theme.WhatMovieNextTheme
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    WhatMovieNextTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MovieList(viewModel)
        }
    }
}

@Composable
fun MovieList(viewModel: MainViewModel) {
    val movies = viewModel.movieList.collectAsState().value
    Column {
        for (movie in movies) {
            MovieListItem(movie)
        }
    }
}

@Composable
fun MovieListItem(movie: Movie) {
    Text(movie.title)
}

@Preview
@Composable
fun MovieListItemPreview() {
    WhatMovieNextTheme {
        MovieListItem(Movie(id = 1, title = "The Silence of the Lambs", watchState = WatchState.PENDING))
    }
}
