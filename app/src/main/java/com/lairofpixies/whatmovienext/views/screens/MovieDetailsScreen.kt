package com.lairofpixies.whatmovienext.views.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.PartialMovie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.Routes

object DetailScreenTags {
    const val TAG_MOVIE_CARD = "MovieCard"
    const val TAG_WATCH_STATE_SWITCH = "WatchStateSwitch"
}

@Composable
fun MovieDetailsScreen(
    movieId: Long?,
    onCloseAction: () -> Unit,
    viewModel: MainViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val partialMovie = movieId?.let { viewModel.getMovie(it).collectAsState().value }

    LaunchedEffect(partialMovie) {
        if (movieId == null || partialMovie is PartialMovie.NotFound) {
            Toast.makeText(context, "Movie not found", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.HOME.route) {
                popUpTo(Routes.HOME.route) {
                    inclusive = true
                }
            }
        }
    }

    if (partialMovie is PartialMovie.Completed) {
        MovieCard(
            movie = partialMovie.movie,
            onCloseAction = onCloseAction,
            onUpdateAction = { viewModel.updateMovieWatched(it.id, it.watchState) },
            onArchiveAction = { viewModel.archiveMovie(it.id) },
        )
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onCloseAction: () -> Unit,
    onUpdateAction: (Movie) -> Unit,
    onArchiveAction: (Movie) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .testTag(DetailScreenTags.TAG_MOVIE_CARD),
    ) {
        TitleField(movie.title)
        WatchStateField(movie.watchState) { watchState ->
            onUpdateAction(movie.copy(watchState = watchState))
        }
        Button(onClick = { onCloseAction() }) {
            Text(stringResource(id = R.string.close))
        }
        Button(onClick = {
            onArchiveAction(movie)
            onCloseAction()
        }) {
            Text(stringResource(id = R.string.archive))
        }
    }
}

@Composable
fun WatchStateField(
    watchState: WatchState,
    switchCallback: (WatchState) -> Unit,
) {
    Row {
        Text(text = watchState.toString())
        Switch(
            modifier = Modifier.testTag(DetailScreenTags.TAG_WATCH_STATE_SWITCH),
            checked = watchState == WatchState.WATCHED,
            onCheckedChange = { watched ->
                switchCallback(if (watched) WatchState.WATCHED else WatchState.PENDING)
            },
        )
    }
}

@Composable
fun TitleField(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
    )
}

@Preview
@Composable
fun DetailScreenPreview() {
    MovieCard(
        Movie(
            id = 1,
            title = "Some like it hot",
            watchState = WatchState.PENDING,
        ),
        onCloseAction = {},
        onUpdateAction = {},
        onArchiveAction = {},
    )
}
