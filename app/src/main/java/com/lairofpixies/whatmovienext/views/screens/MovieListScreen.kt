package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState
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
                items =
                    listOf(
                        filterListItem(listMode, onListModeChanged),
                        CustomBarItem(NavigationItem.CreateMovieShortcut),
                    ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
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

fun filterListItem(
    listMode: ListMode,
    onListModeChanged: (ListMode) -> Unit,
): CustomBarItem {
    val navigationItem =
        when (listMode) {
            ListMode.ALL -> NavigationItem.AllMoviesFilter
            ListMode.PENDING -> NavigationItem.PendingFilter
            ListMode.WATCHED -> NavigationItem.WatchedFilter
        }
    return CustomBarItem(navigationItem) {
        onListModeChanged(listMode.next())
    }
}
