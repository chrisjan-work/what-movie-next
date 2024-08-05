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
package com.lairofpixies.whatmovienext.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AMovie
import com.lairofpixies.whatmovienext.models.data.LoadingAMovie
import com.lairofpixies.whatmovienext.models.data.SearchQuery
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import com.lairofpixies.whatmovienext.views.state.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val movieRepo: MovieRepository,
        private val apiRepo: ApiRepository,
    ) : ScreenViewModel() {
        private val _searchState = MutableStateFlow(SearchState.ENTRY)
        val searchState = _searchState.asStateFlow()

        private val _currentQuery = MutableStateFlow(SearchQuery(""))
        val currentQuery: StateFlow<SearchQuery> = _currentQuery.asStateFlow()

        private val _selectedMovie = MutableStateFlow<LoadingAMovie>(LoadingAMovie.Empty)
        val selectedMovie = _selectedMovie.asStateFlow()

        private val _searchResults: MutableStateFlow<LoadingAMovie> =
            MutableStateFlow(LoadingAMovie.Empty)
        val searchResults: StateFlow<LoadingAMovie> = _searchResults.asStateFlow()

        private var searchJob: Job? = null

        fun switchToSearchEntry() {
            _searchState.value = SearchState.ENTRY
        }

        fun switchToSearchResults() {
            if (searchResults.value is LoadingAMovie.Multiple) {
                _searchState.value = SearchState.RESULTS
            } else {
                switchToSearchEntry()
            }
            updateBusyDisplay(false)
        }

        fun switchToChoiceScreen() {
            if (selectedMovie.value.singleMovieOrNull<AMovie.ForCard>() != null) {
                _searchState.value = SearchState.CHOICE
            } else {
                switchToSearchEntry()
            }
            updateBusyDisplay(false)
        }

        fun updateSearchQuery(query: SearchQuery) {
            _currentQuery.value = query
        }

        fun onSaveQueryAction() {
            TODO()
        }

        fun startSearch() {
            searchJob =
                viewModelScope.launch {
                    clearSearchResults()
                    if (currentQuery.value.title.isBlank()) {
                        showPopup(PopupInfo.EmptyTitle)
                        return@launch
                    }

                    apiRepo.findMoviesByTitle(title = currentQuery.value.title).collect { results ->
                        when (results) {
                            is LoadingAMovie.Loading -> {
                                updateBusyDisplay(true)
                            }

                            is LoadingAMovie.Failed -> {
                                updateBusyDisplay(false)
                                showPopup(PopupInfo.ConnectionFailed)
                                Timber.e("Connection error: ${results.trowable}")
                            }

                            is LoadingAMovie.Empty -> {
                                clearSearchResults()
                                showPopup(PopupInfo.SearchEmpty)
                            }

                            is LoadingAMovie.Single -> {
                                clearSearchResults(false)
                                results.singleMovieOrNull<AMovie.ForSearch>()?.let { movie ->
                                    fetchFromRemote(movie.searchData.tmdbId)
                                } ?: {
                                    updateBusyDisplay(false)
                                    showPopup(PopupInfo.ConnectionFailed)
                                }
                            }

                            is LoadingAMovie.Multiple -> {
                                _searchResults.value = results
                                switchToSearchResults()
                            }
                        }
                    }
                }
        }

        @VisibleForTesting
        fun cancelSearch() {
            searchJob?.cancel()
            searchJob = null
            clearSearchResults()
        }

        private fun clearSearchResults(resetBusyDisplay: Boolean = true) {
            _searchResults.value = LoadingAMovie.Empty
            if (resetBusyDisplay) {
                updateBusyDisplay(false)
            }
        }

        fun fetchFromRemote(tmdbId: Long) {
            viewModelScope.launch {
                apiRepo.getMovieDetails(tmdbId).collect { loadingMovie ->
                    when (loadingMovie) {
                        is LoadingAMovie.Loading -> {
                            updateBusyDisplay(true)
                        }

                        is LoadingAMovie.Failed -> {
                            updateBusyDisplay(false)
                            switchToSearchEntry()
                            showPopup(PopupInfo.ConnectionFailed)
                            Timber.e("Connection error: ${loadingMovie.trowable}")
                        }

                        is LoadingAMovie.Empty -> {
                            updateBusyDisplay(false)
                            switchToSearchEntry()
                            showPopup(PopupInfo.SearchEmpty)
                        }

                        is LoadingAMovie.Single -> {
                            _selectedMovie.value = loadingMovie
                            switchToChoiceScreen()
                        }

                        is LoadingAMovie.Multiple -> {
                            // should never happen:
                            // the api call always returns a single movie or nothing
                            // even though our wrapper can accept multiple movies
                            updateBusyDisplay(false)
                            showPopup(PopupInfo.ConnectionFailed)
                            Timber.e("Connection error: multiple results where only one was expected when fetching movie from backend")
                        }
                    }
                }
            }
        }

        fun onSaveMovieAction() {
            viewModelScope.launch {
                val movieToSave = selectedMovie.value.singleMovieOrNull<AMovie.ForCard>()
                if (movieToSave == null || movieToSave.searchData.tmdbId <= 0) {
                    Timber.e("Attempting to save empty movie")
                    return@launch
                }
                movieRepo.storeMovie(movieToSave).join()
                onLeaveAction()
            }
        }

        private fun updateBusyDisplay(isBusy: Boolean) {
            if (isBusy) {
                showPopup(PopupInfo.Searching { cancelSearch() })
            } else {
                closePopupOfType(PopupInfo.Searching::class)
            }
        }

        fun handleBackButton() {
            when (searchState.value) {
                SearchState.ENTRY -> onLeaveAction()
                SearchState.RESULTS -> switchToSearchEntry()
                SearchState.CHOICE -> {
                    if (searchResults.value is LoadingAMovie.Multiple) {
                        switchToSearchResults()
                    } else {
                        switchToSearchEntry()
                    }
                }
            }
        }
    }
