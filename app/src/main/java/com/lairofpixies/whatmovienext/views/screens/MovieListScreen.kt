package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.ui.theme.WhatMovieNextTheme
import com.lairofpixies.whatmovienext.viewmodel.ListMode
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomNavigationBar
import com.lairofpixies.whatmovienext.views.navigation.NavigationItem

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
    navController: NavController,
) {
    val filteredMovies =
        when (listMode) {
            ListMode.ALL -> movies
            ListMode.WATCHED -> movies.filter { it.watchState == WatchState.WATCHED }
            ListMode.PENDING -> movies.filter { it.watchState == WatchState.PENDING }
        }

    Scaffold(
        bottomBar = {
            CustomNavigationBar(
                navController = navController,
                items = listOf(CustomBarItem(NavigationItem.CreateMovie)),
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            ListModeButton(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .zIndex(1f),
                listMode,
                onListModeChanged,
            )
            LazyColumn(
                modifier = Modifier.testTag(MovieListTags.TAG_MOVIE_LIST),
            ) {
                items(filteredMovies) { movie ->
                    MovieListItem(movie) { onMovieClicked(movie) }
                }
            }
        }
    }
}

@Composable
fun ListModeButton(
    modifier: Modifier = Modifier,
    listMode: ListMode,
    onListModeChanged: (ListMode) -> Unit,
) {
    Button(
        modifier = modifier.testTag(MovieListTags.TAG_MODE_BUTTON),
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
        ListModeButton(listMode = listMode.value) { listMode.value = it }
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
