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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.SearchQuery
import com.lairofpixies.whatmovienext.viewmodels.SearchViewModel
import com.lairofpixies.whatmovienext.views.components.DebugTitle
import com.lairofpixies.whatmovienext.views.components.MovieCard
import com.lairofpixies.whatmovienext.views.components.SearchResultsPicker
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.state.SearchState

@Composable
fun SearchScreen(searchViewModel: SearchViewModel) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val onCloseKeyboard: () -> Unit = {
        softwareKeyboardController?.hide()
        focusManager.clearFocus()
        focusRequester.freeFocus()
    }

    BackHandler(true) {
        searchViewModel.handleBackButton()
    }

    when (searchViewModel.searchState.collectAsState().value) {
        SearchState.ENTRY -> {
            SearchEditor(
                query = searchViewModel.currentQuery.collectAsState().value,
                onUpdateQuery = { searchQuery ->
                    searchViewModel.updateSearchQuery(searchQuery)
                },
                onSearchAction = { searchViewModel.startSearch() },
                onSaveQueryAction = { searchViewModel.onSaveQueryAction() },
                onCancelAction = { searchViewModel.onLeaveAction() },
                onCloseKeyboard = onCloseKeyboard,
                focusRequester = focusRequester,
                modifier = Modifier.testTag(UiTags.Screens.QUERY_EDITOR),
            )
        }

        SearchState.RESULTS -> {
            val searchResults = searchViewModel.searchResults.collectAsState().value
            val scrollState =
                rememberSaveable(saver = LazyListState.Saver) {
                    searchViewModel.resultsScroll
                }
            SearchResultsPicker(
                searchResults = searchResults.movies.toList<Movie.ForSearch>(),
                scrollState = scrollState,
                onResultSelected = { selectedId ->
                    searchViewModel.fetchFromRemote(selectedId)
                },
                modifier = Modifier.testTag(UiTags.Screens.SEARCH_RESULTS),
            )
        }

        SearchState.CHOICE -> {
            val selectedMovie = searchViewModel.selectedMovie.collectAsState().value
            selectedMovie.singleMovieOrNull<Movie.ForCard>()?.let { movie ->
                MovieCard(
                    movie = movie,
                    bottomItems =
                        bottomItemsForChoiceView(
                            onCancelAction = { searchViewModel.onLeaveAction() },
                            onEditSearchAction = { searchViewModel.switchToSearchEntry() },
                            onShowResultsAction = { searchViewModel.switchToSearchResults() },
                            onSaveMovieAction = { searchViewModel.onSaveMovieAction() },
                        ),
                    modifier = Modifier.testTag(UiTags.Screens.SELECTION_VIEW),
                )
            } ?: {
                searchViewModel.switchToSearchEntry()
            }
        }
    }
}

@Composable
fun SearchEditor(
    query: SearchQuery,
    onUpdateQuery: (SearchQuery) -> Unit,
    onSearchAction: () -> Unit,
    onSaveQueryAction: () -> Unit,
    onCancelAction: () -> Unit,
    onCloseKeyboard: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Box {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                CustomBottomBar(
                    items =
                        bottomItemsForSearchEditor(
                            searchEnabled = query.title.isNotBlank(),
                            onCancelAction = onCancelAction,
                            onSaveAction = onSaveQueryAction,
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
                DebugTitle("Search Movie")
                EditableTitleField(
                    query.title,
                    onTitleChanged = { onUpdateQuery(query.copy(title = it)) },
                    onSearchAction = onSearchAction,
                    onCloseKeyboard = onCloseKeyboard,
                    focusRequester = focusRequester,
                )
            }
        }
    }
}

@Composable
fun EditableTitleField(
    title: String,
    onTitleChanged: (String) -> Unit,
    onSearchAction: () -> Unit,
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
                onDone = {
                    onCloseKeyboard()
                    if (title.isNotBlank()) {
                        onSearchAction()
                    }
                },
            ),
    )
}

fun bottomItemsForSearchEditor(
    searchEnabled: Boolean,
    onCancelAction: () -> Unit,
    onSaveAction: () -> Unit,
    onSearchAction: () -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(ButtonSpec.CancelAction, onCancelAction),
        CustomBarItem(ButtonSpec.SaveAction, enabled = false, onClick = onSaveAction),
        CustomBarItem(ButtonSpec.SearchAction, searchEnabled, onClick = onSearchAction),
    )

fun bottomItemsForChoiceView(
    onCancelAction: () -> Unit,
    onEditSearchAction: () -> Unit,
    onShowResultsAction: () -> Unit,
    onSaveMovieAction: () -> Unit,
): List<CustomBarItem> =
    listOf(
        CustomBarItem(ButtonSpec.CancelAction, onCancelAction),
        CustomBarItem(ButtonSpec.EditShortcut, onEditSearchAction),
        CustomBarItem(ButtonSpec.SearchAction, onShowResultsAction),
        CustomBarItem(ButtonSpec.SaveAction, onSaveMovieAction),
    )
