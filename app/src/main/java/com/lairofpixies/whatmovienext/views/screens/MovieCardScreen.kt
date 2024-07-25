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
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.isMissing
import com.lairofpixies.whatmovienext.viewmodels.MovieCardViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes

@Composable
fun MovieCardScreen(
    movieId: Long?,
    cardViewModel: MovieCardViewModel,
) {
    val context = LocalContext.current
    val partialMovie = movieId?.let { cardViewModel.getMovie(it).collectAsState().value }

    LaunchedEffect(partialMovie) {
        if (partialMovie.isMissing()) {
            Toast
                .makeText(context, context.getString(R.string.movie_not_found), Toast.LENGTH_SHORT)
                .show()
            cardViewModel.onCancelAction()
        }
    }

    if (partialMovie is AsyncMovieInfo.Single) {
        MovieCard(
            movie = partialMovie.movie,
            onHomeAction = { cardViewModel.onNavigateTo(Routes.AllMoviesView) },
            onEditAction = { id -> cardViewModel.onNavigateWithParam(Routes.EditMovieView, id) },
            onUpdateAction = { id, watchState -> cardViewModel.updateMovieWatched(id, watchState) },
        )
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onHomeAction: () -> Unit,
    onEditAction: (Long) -> Unit,
    onUpdateAction: (Long, WatchState) -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(UiTags.Screens.MOVIE_CARD),
        bottomBar = {
            CustomBottomBar(
                items =
                    bottomItemsForMovieCard(
                        movie,
                        onHomeAction = onHomeAction,
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
    onEditAction: (Long) -> Unit,
    onUpdateAction: (Long, WatchState) -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(ButtonSpec.MoviesShortcut, onHomeAction),
        if (movie.watchState == WatchState.PENDING) {
            CustomBarItem(ButtonSpec.PendingMovieState) {
                onUpdateAction(movie.id, WatchState.WATCHED)
            }
        } else {
            CustomBarItem(ButtonSpec.WatchedMovieState) {
                onUpdateAction(movie.id, WatchState.PENDING)
            }
        },
        CustomBarItem(ButtonSpec.EditShortcut) { onEditAction(movie.id) },
    )

@Composable
fun TitleField(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
    )
}
