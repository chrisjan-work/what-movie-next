package com.lairofpixies.whatmovienext.views.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.PartialMovie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomNavigationBar
import com.lairofpixies.whatmovienext.views.navigation.NavigationItem
import com.lairofpixies.whatmovienext.views.navigation.Routes

object MovieCardScreenTags {
    const val TAG_MOVIE_CARD = "MovieCard"
}

@Composable
fun MovieCardScreen(
    movieId: Long?,
    onCancelAction: () -> Unit,
    viewModel: MainViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val partialMovie = movieId?.let { viewModel.getMovie(it).collectAsState().value }

    LaunchedEffect(partialMovie) {
        if (movieId == null || partialMovie is PartialMovie.NotFound) {
            Toast.makeText(context, "Movie not found", Toast.LENGTH_SHORT).show()
            onCancelAction()
        }
    }

    if (partialMovie is PartialMovie.Completed) {
        MovieCard(
            movie = partialMovie.movie,
            navController = navController,
            onEditAction = { navController.navigate(Routes.EditMovieView.route(it.id)) },
            onUpdateAction = { viewModel.updateMovieWatched(it.id, it.watchState) },
        )
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    navController: NavController,
    onEditAction: (Movie) -> Unit,
    onUpdateAction: (Movie) -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(MovieCardScreenTags.TAG_MOVIE_CARD),
        bottomBar = {
            CustomNavigationBar(
                navController = navController,
                items =
                    movieCardActionItems(
                        movie,
                        onEditAction = onEditAction,
                        onUpdateAction = onUpdateAction,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        // Todo: use colors from theme
                        color =
                            if (movie.watchState == WatchState.PENDING) {
                                Color.White
                            } else {
                                Color.LightGray
                            },
                    ),
        ) {
            TitleField(movie.title)
        }
    }
}

fun movieCardActionItems(
    movie: Movie,
    onEditAction: (Movie) -> Unit,
    onUpdateAction: (Movie) -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(NavigationItem.AllMovies),
        if (movie.watchState == WatchState.PENDING) {
            CustomBarItem(NavigationItem.MarkAsWatched) {
                onUpdateAction(
                    movie.copy(
                        watchState = WatchState.WATCHED,
                    ),
                )
            }
        } else {
            CustomBarItem(NavigationItem.MarkAsPending) {
                onUpdateAction(
                    movie.copy(
                        watchState = WatchState.PENDING,
                    ),
                )
            }
        },
        CustomBarItem(NavigationItem.Edit) { onEditAction(movie) },
    )

@Composable
fun TitleField(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
    )
}
