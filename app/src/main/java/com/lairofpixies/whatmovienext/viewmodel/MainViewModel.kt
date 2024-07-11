package com.lairofpixies.whatmovienext.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.PartialMovie
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

        private fun refreshMovieList(movies: List<Movie>) {
            _uiState.update { currentState ->
                currentState.copy(
                    movieList = movies,
                )
            }
        }

        fun getMovie(movieId: Long): StateFlow<PartialMovie> = repo.getMovie(movieId)

        fun createMovie(andThen: (Long) -> Unit) =
            viewModelScope.launch {
                val newId = repo.createMovie()
                andThen(newId)
            }

        fun addMovie(title: String) = viewModelScope.launch { repo.addMovie(title) }

        fun addNewMovie(movie: Movie) {
            addMovie(movie.title)
        }

        fun updateMovieWatched(
            movieId: Long,
            watchState: WatchState,
        ) = viewModelScope.launch { repo.setWatchState(movieId, watchState) }

        fun archiveMovie(movieId: Long) = viewModelScope.launch { repo.archiveMovie(movieId) }

        fun setListMode(listMode: ListMode) {
            _uiState.update { it.copy(listMode = listMode) }
        }
    }
