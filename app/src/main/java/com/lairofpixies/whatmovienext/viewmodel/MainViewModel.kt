package com.lairofpixies.whatmovienext.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.PartialMovie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.database.hasQuietSaveableChanges
import com.lairofpixies.whatmovienext.database.hasSaveableChanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val repo: MovieRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(UiState())
        val uiState: StateFlow<UiState> = _uiState.asStateFlow()

        private var currentlyEditing: Movie? = null

        init {
            viewModelScope.launch {
                repo.movies.collect { movies ->
                    refreshMovieList(movies)
                }
            }
        }

        private fun refreshMovieList(movies: List<Movie>) {
            _uiState.update { currentState ->
                currentState.copy(
                    movieList = movies,
                )
            }
        }

        fun getMovie(movieId: Long): StateFlow<PartialMovie> = repo.getMovie(movieId)

        @VisibleForTesting
        suspend fun addMovie(movie: Movie): Long = viewModelScope.async { repo.addMovie(movie) }.await()

        @VisibleForTesting
        suspend fun updateMovie(movie: Movie) = viewModelScope.async { repo.updateMovie(movie) }.await()

        fun updateMovieWatched(
            movieId: Long,
            watchState: WatchState,
        ) = viewModelScope.launch { repo.setWatchState(movieId, watchState) }

        fun archiveMovie(movieId: Long) = viewModelScope.launch { repo.archiveMovie(movieId) }

        fun setListMode(listMode: ListMode) {
            _uiState.update { it.copy(listMode = listMode) }
        }

        fun beginEditing(currentMovie: Movie? = null) {
            currentlyEditing = currentMovie
        }

        fun saveMovie(
            movie: Movie,
            onSuccess: (id: Long) -> Unit,
            onFailure: (errorState: ErrorState) -> Unit,
        ) {
            viewModelScope.launch {
                if (movie.title.isBlank()) {
                    onFailure(ErrorState.SavingWithEmptyTitle)
                    return@launch
                }

                val expectedId = findExistingMovieId(movie)
                val newId =
                    if (expectedId == 0L) {
                        addMovie(movie)
                    } else {
                        updateMovie(movie.copy(id = expectedId))
                    }

                currentlyEditing = movie.copy(id = newId)
                onSuccess(newId)
            }
        }

        @VisibleForTesting
        suspend fun findExistingMovieId(movie: Movie): Long =
            viewModelScope
                .async {
                    val foundMovie =
                        repo.fetchMovieById(movie.id)
                    return@async foundMovie?.id ?: 0L
                }.await()

        fun showError(errorState: ErrorState) {
            _uiState.update { it.copy(errorState = errorState) }
        }

        fun clearError() {
            _uiState.update { it.copy(errorState = ErrorState.None) }
        }

        fun hasSaveableChanges(potentialMovie: Movie) = potentialMovie.hasSaveableChanges(currentlyEditing)

        fun hasQuietSaveableChanges(potentialMovie: Movie) = potentialMovie.hasQuietSaveableChanges(currentlyEditing)
    }
