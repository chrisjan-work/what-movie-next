package com.lairofpixies.whatmovienext.views.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.viewmodel.ErrorState
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel

object EditableDetailScreenTags {
    const val TAG_EDITABLE_MOVIE_CARD = "EditableMovieCard"
}

@Composable
fun EditableMovieDetailsScreen(
    movieId: Long?,
    onCloseAction: () -> Unit,
    viewModel: MainViewModel,
    navController: NavController,
) {
    val editableMovie =
        remember {
            val movie = Movie(title = "")
            mutableStateOf(
                movie,
            )
        }

    // run only once when starting the screen
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        viewModel.beginEditing()
        focusRequester.requestFocus()
    }

    val onSaveAction = {
        viewModel.saveMovie(
            editableMovie.value,
            onSuccess = onCloseAction,
            onFailure = { viewModel.showError(it) },
        )
    }

    BackHandler(true) {
        when {
            viewModel.hasSaveableChanges(editableMovie.value) ->
                viewModel.showError(
                    ErrorState.UnsavedChanges(
                        onSave = onSaveAction,
                        onDiscard = onCloseAction,
                    ),
                )

            viewModel.hasQuietSaveableChanges(editableMovie.value) ->
                onSaveAction()

            else -> onCloseAction()
        }
    }

    EditableMovieCard(
        movieState = editableMovie,
        focusRequester = focusRequester,
        onSaveAction = onSaveAction,
    )
}

@Composable
fun EditableMovieCard(
    movieState: MutableState<Movie>,
    focusRequester: FocusRequester,
    onSaveAction: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .testTag(EditableDetailScreenTags.TAG_EDITABLE_MOVIE_CARD),
    ) {
        EditableTitleField(movieState.value.title, focusRequester) {
            movieState.value = movieState.value.copy(title = it)
        }
        Button(
            onClick = { onSaveAction() },
        ) {
            Text(stringResource(id = R.string.save_and_close))
        }
    }
}

@Composable
fun EditableTitleField(
    title: String,
    focusRequester: FocusRequester,
    onTitleChanged: (String) -> Unit,
) {
    TextField(
        value = title,
        onValueChange = onTitleChanged,
        label = { Text(stringResource(id = R.string.title)) },
        modifier = Modifier.focusRequester(focusRequester),
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = { focusRequester.freeFocus() },
            ),
    )
}
