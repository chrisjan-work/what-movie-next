package com.lairofpixies.whatmovienext.views.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.PartialMovie
import com.lairofpixies.whatmovienext.database.isNew
import com.lairofpixies.whatmovienext.viewmodel.ErrorState
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomNavigationBar
import com.lairofpixies.whatmovienext.views.navigation.NavigationItem

object EditCardScreenTags {
    const val TAG_EDITABLE_MOVIE_CARD = "EditCard"
}

@Composable
fun EditCardScreen(
    movieId: Long?,
    onCloseWithIdAction: (Long) -> Unit,
    onCancelAction: () -> Unit,
    viewModel: MainViewModel,
    navController: NavController,
) {
    // run only once when starting the screen
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        viewModel.beginEditing()
        focusRequester.requestFocus()
    }

    val partialMovie = movieId?.let { viewModel.getMovie(it).collectAsState().value }
    val editableMovie =
        remember {
            mutableStateOf(
                Movie(title = ""),
            )
        }

    LaunchedEffect(partialMovie) {
        if (partialMovie is PartialMovie.Completed) {
            editableMovie.value = partialMovie.movie
            viewModel.beginEditing(partialMovie.movie)
        }
    }

    val onSaveAction = {
        editableMovie.value = editableMovie.value.copy(title = editableMovie.value.title.trim())
        viewModel.saveMovie(
            editableMovie.value,
            onSuccess = { savedId ->
                editableMovie.value = editableMovie.value.copy(id = savedId)
                onCloseWithIdAction(savedId)
            },
            onFailure = { viewModel.showError(it) },
        )
    }

    val onArchiveAction = {
        viewModel.archiveMovie(editableMovie.value.id)
        onCancelAction()
    }

    BackHandler(true) {
        when {
            viewModel.hasSaveableChanges(editableMovie.value) ->
                viewModel.showError(
                    ErrorState.UnsavedChanges(
                        onSave = onSaveAction,
                        onDiscard = { onCloseWithIdAction(editableMovie.value.id) },
                    ),
                )

            viewModel.hasQuietSaveableChanges(editableMovie.value) ->
                onSaveAction()

            else -> {
                onCloseWithIdAction(editableMovie.value.id)
            }
        }
    }

    EditCard(
        movieState = editableMovie,
        focusRequester = focusRequester,
        navController = navController,
        onCancelAction = onCancelAction,
        onSaveAction = onSaveAction,
        onArchiveAction = onArchiveAction,
    )
}

@Composable
fun EditCard(
    movieState: MutableState<Movie>,
    focusRequester: FocusRequester,
    navController: NavController,
    onCancelAction: () -> Unit,
    onSaveAction: () -> Unit,
    onArchiveAction: () -> Unit,
) {
    val creating = movieState.value.isNew()
    Scaffold(
        modifier = Modifier.testTag(EditCardScreenTags.TAG_EDITABLE_MOVIE_CARD),
        bottomBar = {
            CustomNavigationBar(
                navController = navController,
                items =
                    listOf(
                        if (creating) {
                            CustomBarItem(NavigationItem.Cancel, onCancelAction)
                        } else {
                            CustomBarItem(NavigationItem.ArchiveAction, onArchiveAction)
                        },
                        CustomBarItem(NavigationItem.SaveChanges, onSaveAction),
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
            EditableTitleField(movieState.value.title, focusRequester) {
                movieState.value = movieState.value.copy(title = it)
            }
        }
    }
}

@Composable
fun EditableTitleField(
    title: String,
    focusRequester: FocusRequester,
    onTitleChanged: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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
                onDone = {
                    keyboardController?.hide()
                    focusRequester.freeFocus()
                    focusManager.clearFocus()
                },
            ),
    )
}
