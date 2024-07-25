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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.toList
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
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
    filteredMovies: List<Movie>,
    onMovieClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = modifier.testTag(UiTags.Screens.MOVIE_LIST),
        ) {
            items(filteredMovies) { movie ->
                MovieListItem(movie) { onMovieClicked(movie.id) }
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
