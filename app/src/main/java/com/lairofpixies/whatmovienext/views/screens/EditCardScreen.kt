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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.isNew
import com.lairofpixies.whatmovienext.viewmodels.EditCardViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar

@Composable
fun EditCardScreen(
    movieId: Long?,
    editViewModel: EditCardViewModel,
) {
    val currentMovie = editViewModel.currentMovie.collectAsState()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(movieId) {
        focusRequester.requestFocus()
        movieId?.let {
            editViewModel.loadMovieForEdit(movieId)
        }
    }

    val onSearchAction = {
        TODO()
    }

    BackHandler(true) {
        editViewModel.handleBackButton()
    }

    EditCard(
        currentMovie = currentMovie.value,
        onUpdateEdits = { editViewModel.updateMovieEdits { it } },
        focusRequester = focusRequester,
        onCancelAction = { editViewModel.onCancelAction() },
        onSaveAction = { editViewModel.onSaveAction() },
        onArchiveAction = {
            editViewModel.archiveCurrentMovie()
            editViewModel.onCancelAction()
        },
        onSearchAction = onSearchAction,
    )
}

@Composable
fun EditCard(
    currentMovie: Movie,
    onUpdateEdits: (Movie) -> Unit,
    focusRequester: FocusRequester,
    onCancelAction: () -> Unit,
    onSaveAction: () -> Unit,
    onArchiveAction: () -> Unit,
    onSearchAction: () -> Unit,
) {
    val creating = currentMovie.isNew()
    Scaffold(
        modifier = Modifier.testTag(UiTags.Screens.EDIT_CARD),
        bottomBar = {
            CustomBottomBar(
                items =
                    bottomItemsForEditCard(
                        creating,
                        searchEnabled = currentMovie.title.isNotBlank(),
                        onCancelAction = onCancelAction,
                        onSaveAction = onSaveAction,
                        onArchiveAction = onArchiveAction,
                        onSearchAction = onSearchAction,
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
            EditableTitleField(
                currentMovie.title,
                focusRequester,
            ) {
                onUpdateEdits(currentMovie.copy(title = it))
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

fun bottomItemsForEditCard(
    creating: Boolean,
    searchEnabled: Boolean,
    onCancelAction: () -> Unit,
    onArchiveAction: () -> Unit,
    onSaveAction: () -> Unit,
    onSearchAction: () -> Unit,
): List<CustomBarItem> =
    listOf(
        if (creating) {
            CustomBarItem(ButtonSpec.CancelAction, onCancelAction)
        } else {
            CustomBarItem(ButtonSpec.ArchiveAction, onArchiveAction)
        },
        CustomBarItem(ButtonSpec.SearchAction, searchEnabled, onClick = onSearchAction),
        CustomBarItem(ButtonSpec.SaveAction, onSaveAction),
    )
