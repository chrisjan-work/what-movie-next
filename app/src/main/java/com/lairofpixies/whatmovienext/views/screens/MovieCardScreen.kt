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
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.isMissing
import com.lairofpixies.whatmovienext.viewmodels.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes

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
        if (movieId == null || partialMovie.isMissing()) {
            Toast.makeText(context, context.getString(R.string.movie_not_found), Toast.LENGTH_SHORT).show()
            onCancelAction()
        }
    }

    if (partialMovie is AsyncMovieInfo.Single) {
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
        modifier = Modifier.testTag(UiTags.Screens.MOVIE_CARD),
        bottomBar = {
            CustomBottomBar(
                items =
                    bottomItemsForMovieCard(
                        movie,
                        onHomeAction = { navController.navigate(Routes.AllMoviesView.route) },
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

fun bottomItemsForMovieCard(
    movie: Movie,
    onHomeAction: () -> Unit,
    onEditAction: (Movie) -> Unit,
    onUpdateAction: (Movie) -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(ButtonSpec.MoviesShortcut, onHomeAction),
        if (movie.watchState == WatchState.PENDING) {
            CustomBarItem(ButtonSpec.PendingMovieState) {
                onUpdateAction(
                    movie.copy(
                        watchState = WatchState.WATCHED,
                    ),
                )
            }
        } else {
            CustomBarItem(ButtonSpec.WatchedMovieState) {
                onUpdateAction(
                    movie.copy(
                        watchState = WatchState.PENDING,
                    ),
                )
            }
        },
        CustomBarItem(ButtonSpec.EditShortcut) { onEditAction(movie) },
    )

@Composable
fun TitleField(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
    )
}
