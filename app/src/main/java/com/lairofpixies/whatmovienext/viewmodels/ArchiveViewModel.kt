package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel
    @Inject
    constructor(
        private val repo: MovieRepository,
    ) : ScreenViewModel() {
        private val _archivedMovies = MutableStateFlow<AsyncMovieInfo>(AsyncMovieInfo.Loading)
        val archivedMovies: StateFlow<AsyncMovieInfo> = _archivedMovies.asStateFlow()

        private val _selection = MutableStateFlow(emptySet<Movie>())
        val selection: StateFlow<Set<Movie>> = _selection.asStateFlow()

        init {
            viewModelScope.launch {
                repo.archivedMovies.collect { movieInfo ->
                    _archivedMovies.value = movieInfo
                }
            }
        }

        fun select(movie: Movie) {
            _selection.value = selection.value + movie
        }

        fun deselect(movie: Movie) {
            _selection.value = selection.value - movie
        }

        fun restoreSelectedMovies() =
            viewModelScope.launch {
                selection.value.forEach {
                    repo.restoreMovie(it.id)
                }
                _selection.value = emptySet()
            }

        fun deleteSelectedMovies() {
            mainViewModel.showError(
                ErrorState.ConfirmDeletion {
                    viewModelScope.launch {
                        selection.value.forEach {
                            repo.deleteMovie(it)
                        }
                    }
                    _selection.value = emptySet()
                },
            )
        }
    }
