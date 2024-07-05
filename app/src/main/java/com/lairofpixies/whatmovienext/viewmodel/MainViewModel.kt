package com.lairofpixies.whatmovienext.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.WatchState
import dagger.hilt.android.lifecycle.HiltViewModel
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

        init {
            viewModelScope.launch {
                repo.movies.collect { movies ->
                    refreshMovieList(movies)
                }
            }
        }

        // TODO: tests
        private fun refreshMovieList(movies: List<Movie>) {
            val expandedMovieUpdate =
                _uiState.value.expandedMovie?.id?.let { expandedMovieId ->
                    movies.firstOrNull { movie -> movie.id == expandedMovieId }
                }

            _uiState.update { currentState ->
                currentState.copy(
                    movieList = movies,
                    expandedMovie = expandedMovieUpdate,
                )
            }
        }

        fun addMovie(title: String) {
            repo.addMovie(title)
        }

        fun expandMovieAction(movie: Movie) {
            _uiState.update { it.copy(expandedMovie = movie) }
        }

        fun closeMovieAction() {
            _uiState.update { it.copy(expandedMovie = null) }
        }

        fun updateMovieWatched(
            movieId: Int,
            watchState: WatchState,
        ) {
            repo.setWatchState(movieId, watchState)
        }

        fun archiveMovieAction(movieId: Int) {
            repo.archiveMovie(movieId)
        }

        fun setListMode(listMode: ListMode) {
            _uiState.update { it.copy(listMode = listMode) }
        }
    }
