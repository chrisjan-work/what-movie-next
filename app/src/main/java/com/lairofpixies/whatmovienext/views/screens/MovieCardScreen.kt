package com.lairofpixies.whatmovienext.views.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
    const val TAG_WATCH_STATE_SWITCH = "WatchStateSwitch"
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
                    listOf(
                        CustomBarItem(NavigationItem.AllMovies),
                        CustomBarItem(NavigationItem.Edit) { onEditAction(movie) },
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            TitleField(movie.title)
            WatchStateField(movie.watchState) { watchState ->
                onUpdateAction(movie.copy(watchState = watchState))
            }
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
            modifier = Modifier.testTag(MovieCardScreenTags.TAG_WATCH_STATE_SWITCH),
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
