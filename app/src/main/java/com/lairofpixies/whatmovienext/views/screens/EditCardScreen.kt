/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.lairofpixies.whatmovienext.views.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.isNew
import com.lairofpixies.whatmovienext.models.data.toList
import com.lairofpixies.whatmovienext.viewmodels.EditCardViewModel
import com.lairofpixies.whatmovienext.views.components.DebugTitle
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar

@Composable
fun EditCardScreen(
    movieId: Long?,
    editViewModel: EditCardViewModel,
) {
    val currentMovie = editViewModel.currentMovie.collectAsState()
    val searchResults = editViewModel.searchResults.collectAsState()

    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val onCloseKeyboard: () -> Unit = {
        softwareKeyboardController?.hide()
        focusManager.clearFocus()
        focusRequester.freeFocus()
    }

    LaunchedEffect(movieId) {
        movieId?.let {
            editViewModel.loadMovieForEdit(movieId)
        }
    }

    BackHandler(true) {
        editViewModel.handleBackButton()
    }

    EditCard(
        currentMovie = currentMovie.value,
        searchResults = searchResults.value.toList(),
        onUpdateEdits = { editViewModel.updateMovieEdits { it } },
        onCancelAction = { editViewModel.onCancelAction() },
        onSaveAction = { editViewModel.onSaveAction() },
        onArchiveAction = {
            editViewModel.archiveCurrentMovie()
            editViewModel.onCancelAction()
        },
        onSearchAction = {
            onCloseKeyboard()
            editViewModel.startSearch()
        },
        onSearchResultSelected = {
            editViewModel.updateMovieEdits { it }
            editViewModel.clearSearchResults()
        },
        onCloseKeyboard = onCloseKeyboard,
        focusRequester = focusRequester,
    )
}

@Composable
fun EditCard(
    currentMovie: Movie,
    searchResults: List<Movie>,
    onUpdateEdits: (Movie) -> Unit,
    onCancelAction: () -> Unit,
    onSaveAction: () -> Unit,
    onArchiveAction: () -> Unit,
    onSearchAction: () -> Unit,
    onSearchResultSelected: (Movie) -> Unit,
    onCloseKeyboard: () -> Unit,
    focusRequester: FocusRequester,
) {
    val creating = currentMovie.isNew()
    Box {
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
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
            ) {
                DebugTitle("Edit Movie")
                EditableTitleField(
                    currentMovie.title,
                    onTitleChanged = { onUpdateEdits(currentMovie.copy(title = it)) },
                    onCloseKeyboard = onCloseKeyboard,
                    focusRequester = focusRequester,
                )
            }
        }

        // should overlap editor, including bottom bar
        SearchResultsPicker(
            searchResults,
            onSearchResultSelected,
        )
    }
}

@Composable
fun EditableTitleField(
    title: String,
    onTitleChanged: (String) -> Unit,
    onCloseKeyboard: () -> Unit,
    focusRequester: FocusRequester,
) {
    // start focused by default
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // update cursor and re-focus if title changed externally
    val textFieldValue = remember { mutableStateOf(TextFieldValue(title)) }
    LaunchedEffect(title) {
        if (title != textFieldValue.value.text) {
            textFieldValue.value =
                textFieldValue.value.copy(
                    text = title,
                    selection = TextRange(title.length),
                )
        }
    }

    TextField(
        value = textFieldValue.value,
        onValueChange = {
            textFieldValue.value = it
            onTitleChanged(it.text)
        },
        label = { Text(stringResource(id = R.string.title)) },
        modifier = Modifier.focusRequester(focusRequester),
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = { onCloseKeyboard() },
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

@Composable
fun SearchResultsPicker(
    searchResults: List<Movie>,
    onResultSelected: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (searchResults.isEmpty()) {
        return
    }
    Column(
        modifier =
            modifier
                .testTag(UiTags.Screens.SEARCH_RESULTS)
                .fillMaxSize()
                .background(Color.White),
    ) {
        DebugTitle(title = "Search Results")
        LazyColumn(
            modifier = modifier,
        ) {
            items(searchResults) { movie ->
                SearchResultItem(
                    movie,
                    onClick = {
                        onResultSelected(movie)
                    },
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    movie: Movie,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .border(border = BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(4.dp))
                .padding(4.dp),
    ) {
        // TODO: Coil image loading (will need a proper url tho)
//        Image(
//            painter = painterResource(id = movie.poster),
//            contentDescription = null,
//            modifier = Modifier.size(100.dp),
//        )
        Spacer(modifier = Modifier.size(16.dp))
        Column {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (movie.originalTitle.isNotBlank() && movie.originalTitle != movie.title) {
                Text(
                    text = movie.originalTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            }
            movie.year?.let { year ->
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            // TODO
//            if (movie.genres.isNotEmpty()) {
//                Text(
//                    text = movie.genres.joinToString(" / "),
//                    style = MaterialTheme.typography.bodySmall,
//                )
//            }
        }
    }
}
