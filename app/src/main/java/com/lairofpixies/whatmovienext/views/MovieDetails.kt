package com.lairofpixies.whatmovienext.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState

object DetailScreenTags {
    const val TAG_MOVIE_CARD = "MovieCard"
    const val TAG_WATCH_STATE_SWITCH = "WatchStateSwitch"
}

@Composable
fun MovieDetails(
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
        WatchStateField(movie.watchState) { newWatchstate ->
            onUpdateAction(movie.copy(watchState = newWatchstate))
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
    MovieDetails(
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
