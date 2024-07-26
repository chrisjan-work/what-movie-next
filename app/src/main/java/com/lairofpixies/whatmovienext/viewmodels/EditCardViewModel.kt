package com.lairofpixies.whatmovienext.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.hasQuietSaveableChangesSince
import com.lairofpixies.whatmovienext.models.data.hasSaveableChangesSince
import com.lairofpixies.whatmovienext.models.database.MovieRepository
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

        private val _searchResults: MutableStateFlow<AsyncMovieInfo> =
            MutableStateFlow(AsyncMovieInfo.Empty)
        val searchResults: StateFlow<AsyncMovieInfo> = _searchResults.asStateFlow()
        private var searchJob: Job? = null
        private val shouldShowSearchPopup: Flow<Boolean> =
            searchResults.map {
                it is AsyncMovieInfo.Loading
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

        fun archiveCurrentMovie() =
            viewModelScope.launch {
                movieRepo.archiveMovie(currentMovie.value.id)
            }

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
                                        movieRepo.deleteMovie(movie)
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

        // detect if there are unsaved changes, prompt the user or save quietly if necessary
        fun handleBackButton() {
            when {
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
                            is AsyncMovieInfo.Loading ->
                                _searchResults.value = AsyncMovieInfo.Loading

                            is AsyncMovieInfo.Failed -> {
                                clearSearchResults()
                                showPopup(PopupInfo.SearchFailed)
                                // TODO: log error remotely
                                Timber.e("Connection error: ${results.trowable}")
                            }

                            is AsyncMovieInfo.Empty -> {
                                clearSearchResults()
                                showPopup(PopupInfo.SearchEmpty)
                            }

                            is AsyncMovieInfo.Single -> {
                                _currentMovie.value = results.movie
                                clearSearchResults()
                            }

                            is AsyncMovieInfo.Multiple -> _searchResults.value = results
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
            _searchResults.value = AsyncMovieInfo.Empty
        }
    }
