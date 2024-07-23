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
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.ListMode

@Composable
fun MovieListScreen(
    listMode: ListMode,
    movies: List<Movie>,
    isArchiveVisitable: Boolean,
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
            CustomBottomBar(
                items =
                    bottomItemsForMovieList(
                        listMode,
                        onListModeChanged,
                        isArchiveVisitable,
                        navController,
                    ),
            )
        },
    ) { innerPadding ->
        MovieList(
            filteredMovies = filteredMovies,
            onMovieClicked = onMovieClicked,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Composable
fun MovieList(
    filteredMovies: List<Movie>,
    onMovieClicked: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = modifier.testTag(UiTags.Screens.MOVIE_LIST),
        ) {
            items(filteredMovies) { movie ->
                MovieListItem(movie) { onMovieClicked(movie) }
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

fun bottomItemsForMovieList(
    listMode: ListMode,
    onListModeChanged: (ListMode) -> Unit,
    isArchiveVisitable: Boolean,
    navController: NavController,
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

    val createItem =
        CustomBarItem(ButtonSpec.CreateMovieShortcut) {
            navController.navigate(Routes.CreateMovieView.route)
        }

    val archiveItem =
        if (isArchiveVisitable) {
            CustomBarItem(ButtonSpec.ArchiveShortcut) {
                navController.navigate(Routes.ArchiveView.route)
            }
        } else {
            null
        }

    return listOfNotNull(
        filterItem,
        createItem,
        archiveItem,
    )
}
