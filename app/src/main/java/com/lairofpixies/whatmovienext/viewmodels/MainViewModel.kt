package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ErrorState
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.UiState
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

        fun setListMode(listMode: ListMode) {
            _uiState.update { it.copy(listMode = listMode) }
        }

        fun showError(errorState: ErrorState) {
            _uiState.update { it.copy(errorState = errorState) }
        }

        fun clearError() {
            _uiState.update { it.copy(errorState = ErrorState.None) }
        }
    }
