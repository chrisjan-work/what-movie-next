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
package com.lairofpixies.whatmovienext.views.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.viewmodels.SearchViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.screens.card.MovieCard
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
                onBottomReached = { searchViewModel.continueSearch() },
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
