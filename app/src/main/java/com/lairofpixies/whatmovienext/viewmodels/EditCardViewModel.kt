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
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.hasMovie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.mappers.MovieMapper
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditCardViewModel
    @Inject
    constructor(
        private val movieRepo: MovieRepository,
        private val apiRepo: ApiRepository,
    ) : ScreenViewModel() {
        private var lastSavedMovie: Movie? = null

        private val _currentMovie = MutableStateFlow(Movie(id = Movie.NEW_ID, title = ""))
        val currentMovie: StateFlow<Movie> = _currentMovie.asStateFlow()

        private val _searchResults: MutableStateFlow<LoadingAMovie> =
            MutableStateFlow(LoadingAMovie.Empty)
        val searchResults: StateFlow<LoadingAMovie> = _searchResults.asStateFlow()
        private var searchJob: Job? = null
        private val shouldShowSearchPopup: Flow<Boolean> =
            searchResults.map {
                it is LoadingAMovie.Loading
            }

        override fun attachMainViewModel(mainViewModel: MainViewModel) {
            super.attachMainViewModel(mainViewModel)
            connectSearchPopupToSearchResults()
        }

        // fetch a movie from the DB, store in-memory for editing
        fun loadMovieForEdit(movieId: Long) =
            viewModelScope.launch {
                movieRepo.fetchMovieById(movieId)?.let { movieFromDb ->
                    updateMovieEdits(resetSaved = true) { movieFromDb }
                }
            }

        // update in-memory movie with edits coming from UI
        fun updateMovieEdits(
            resetSaved: Boolean = false,
            movie: Movie.() -> Movie,
        ) {
            val editsToStore = movie.invoke(_currentMovie.value)
            if (resetSaved) {
                lastSavedMovie = editsToStore
            }
            _currentMovie.value = editsToStore
        }

        @VisibleForTesting
        suspend fun addMovieToDb(movie: Movie): Long =
            viewModelScope
                .async {
                    movieRepo.addMovie(movie)
                }.await()

        @VisibleForTesting
        suspend fun updateMovieInDb(movie: Movie) =
            viewModelScope
                .async {
                    movieRepo.updateMovie(movie)
                }.await()

        // save currently edited movie in DB
        fun onSaveAction() {
            viewModelScope.launch {
                // trim title before saving
                val movie = currentMovie.value.run { copy(title = title.trim()) }

                val onSuccess = { storedId: Long ->
                    updateMovieEdits(resetSaved = true) { movie.copy(id = storedId) }
                    onCloseWithIdAction(storedId)
                }

                // reject movies with empty titles
                if (movie.title.isBlank()) {
                    showPopup(PopupInfo.EmptyTitle)
                    return@launch
                }

                val isMovieAlreadyInDb = movieRepo.fetchMovieById(movie.id) != null
                val duplicateMovie =
                    movieRepo.fetchMoviesByTitle(movie.title).firstOrNull { it.id != movie.id }

                // if a movie with the same title exists, offer to overwrite it or discard edits
                if (duplicateMovie != null) {
                    val errorInfo =
                        PopupInfo.DuplicatedTitle(
                            onSave = {
                                viewModelScope.launch {
                                    val movieToUpdate = movie.copy(id = duplicateMovie.id)
                                    if (isMovieAlreadyInDb) {
                                        movieRepo.deleteMovie(movie.id)
                                    }
                                    onSuccess(updateMovieInDb(movieToUpdate))
                                }
                            },
                            onDiscard = {
                                // nothing to do: changes are discarded
                                onSuccess(movie.id)
                            },
                        )
                    showPopup(errorInfo)
                    return@launch
                }

                // save or overwrite
                onSuccess(
                    if (isMovieAlreadyInDb) {
                        updateMovieInDb(movie)
                    } else {
                        addMovieToDb(movie)
                    },
                )
            }
        }

        // close search results if open, or else..
        // detect if there are unsaved changes, prompt the user or save quietly if necessary
        fun handleBackButton() {
            when {
                searchResults.value.hasMovie() -> clearSearchResults()
                currentMovie.value.hasSaveableChangesSince(lastSavedMovie) ->
                    showPopup(
                        PopupInfo.UnsavedChanges(
                            onSave = { onSaveAction() },
                            onDiscard = { onCloseWithIdAction(currentMovie.value.id) },
                        ),
                    )

                currentMovie.value.hasQuietSaveableChangesSince(lastSavedMovie) ->
                    onSaveAction()

                else -> {
                    onCloseWithIdAction(currentMovie.value.id)
                }
            }
        }

        fun startSearch() {
            searchJob =
                viewModelScope.launch {
                    if (currentMovie.value.title.isBlank()) {
                        clearSearchResults()
                        showPopup(PopupInfo.EmptyTitle)
                        return@launch
                    }

                    apiRepo.findMoviesByTitle(title = currentMovie.value.title).collect { results ->
                        when (results) {
                            is LoadingAMovie.Loading -> {
                                _searchResults.value = LoadingAMovie.Loading
                            }

                            is LoadingAMovie.Failed -> {
                                clearSearchResults()
                                showPopup(PopupInfo.ConnectionFailed)
                                // TODO: log error remotely
                                Timber.e("Connection error: ${results.trowable}")
                            }

                            is LoadingAMovie.Empty -> {
                                clearSearchResults()
                                showPopup(PopupInfo.SearchEmpty)
                            }

                            is LoadingAMovie.Single -> {
                                fetchFromRemote(results.movie.searchData?.tmdbId)
                                clearSearchResults()
                            }

                            is LoadingAMovie.Multiple -> _searchResults.value = results
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

        private fun connectSearchPopupToSearchResults() {
            viewModelScope.launch {
                shouldShowSearchPopup.collect { isLoading ->
                    if (isLoading) {
                        showPopup(PopupInfo.Searching { cancelSearch() })
                    } else {
                        closePopupOfType(PopupInfo.Searching::class)
                    }
                }
            }
        }

        fun clearSearchResults() {
            _searchResults.value = LoadingAMovie.Empty
        }

        fun fetchFromRemote(tmdbId: Long?) {
            if (tmdbId == null) {
                showPopup(PopupInfo.SearchEmpty)
                return
            }

            viewModelScope.launch {
                apiRepo.getMovieDetails(tmdbId).collect { loadingMovie ->
                    when (loadingMovie) {
                        // TODO: show loading state
                        // is LoadingMovie.Loading
                        is LoadingAMovie.Failed -> {
                            showPopup(PopupInfo.ConnectionFailed)
                            Timber.e("Connection error: ${loadingMovie.trowable}")
                        }

                        is LoadingAMovie.Single -> {
                            // TODO: should not need to map it
                            _currentMovie.value =
                                MovieMapper().toMovie(loadingMovie.movie as AMovie.ForCard)
                        }

                        else -> {}
                    }
                }
            }
        }
    }
