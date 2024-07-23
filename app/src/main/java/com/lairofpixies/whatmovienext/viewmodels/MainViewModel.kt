package com.lairofpixies.whatmovienext.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.hasQuietSaveableChanges
import com.lairofpixies.whatmovienext.models.data.hasSaveableChanges
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ErrorState
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.UiState
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
            viewModelScope.launch {
                repo.archivedMovies.collect { movies ->
                    refreshArchive(movies)
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

        private fun refreshArchive(movies: List<Movie>) {
            _uiState.update { currentState ->
                currentState.copy(
                    archiveList = movies,
                )
            }
        }

        fun getMovie(movieId: Long): StateFlow<AsyncMovieInfo> = repo.getMovie(movieId)

        @VisibleForTesting
        suspend fun addMovie(movie: Movie): Long =
            viewModelScope
                .async {
                    repo.addMovie(movie).also { newId ->
                        // TODO: verify in test
                        currentlyEditing = movie.copy(id = newId)
                    }
                }.await()

        @VisibleForTesting
        suspend fun updateMovie(movie: Movie) =
            viewModelScope
                .async {
                    repo.updateMovie(movie).also {
                        currentlyEditing = movie
                    }
                }.await()

        fun updateMovieWatched(
            movieId: Long,
            watchState: WatchState,
        ) = viewModelScope.launch { repo.setWatchState(movieId, watchState) }

        // Archive
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

                val isMovieAlreadyInDb = repo.fetchMovieById(movie.id) != null
                val duplicateMovie =
                    repo.fetchMoviesByTitle(movie.title).firstOrNull { it.id != movie.id }

                if (duplicateMovie != null) {
                    val error =
                        ErrorState.DuplicatedTitle(
                            onSave = {
                                viewModelScope.launch {
                                    val movieToUpdate = movie.copy(id = duplicateMovie.id)
                                    if (isMovieAlreadyInDb) {
                                        repo.deleteMovie(movie)
                                    }
                                    onSuccess(updateMovie(movieToUpdate))
                                }
                            },
                            onDiscard = {
                                // nothing to do: changes are discarded
                                onSuccess(movie.id)
                            },
                        )
                    onFailure(error)
                    return@launch
                }

                if (isMovieAlreadyInDb) {
                    onSuccess(updateMovie(movie))
                } else {
                    onSuccess(addMovie(movie))
                }
            }
        }

        fun showError(errorState: ErrorState) {
            _uiState.update { it.copy(errorState = errorState) }
        }

        fun clearError() {
            _uiState.update { it.copy(errorState = ErrorState.None) }
        }

        fun hasSaveableChanges(potentialMovie: Movie) = potentialMovie.hasSaveableChanges(currentlyEditing)

        fun hasQuietSaveableChanges(potentialMovie: Movie) = potentialMovie.hasQuietSaveableChanges(currentlyEditing)
    }
