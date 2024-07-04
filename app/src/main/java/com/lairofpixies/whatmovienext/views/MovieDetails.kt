package com.lairofpixies.whatmovienext.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState

object DetailScreenTags {
    const val TAG_MOVIE_CARD = "MovieCard"
}

@Composable
fun MovieDetails(
    movie: Movie,
    onCloseAction: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .testTag(DetailScreenTags.TAG_MOVIE_CARD)
                .clickable { onCloseAction() },
    ) {
        TitleField(movie.title)
    }
}

@Composable
fun TitleField(title: String) {
    Text(
        text = title,
        color = Color.Red,
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
    ) {}
}
