package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.util.mapState
import com.lairofpixies.whatmovienext.views.state.ListMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel
    @Inject
    constructor(
        private val repo: MovieRepository,
    ) : ScreenViewModel() {
        private val _listedMovies = MutableStateFlow<AsyncMovieInfo>(AsyncMovieInfo.Loading)
        val listedMovies: StateFlow<AsyncMovieInfo> = _listedMovies.asStateFlow()

        private val _hasArchivedMovies = MutableStateFlow(false)
        val hasArchivedMovies: StateFlow<Boolean> = _hasArchivedMovies.asStateFlow()

        lateinit var listMode: StateFlow<ListMode>
            private set

        init {
            viewModelScope.launch {
                repo.archivedMovies.collect { movies ->
                    _hasArchivedMovies.value = movies.isNotEmpty()
                }
            }
        }

        override fun attachMainViewModel(mainViewModel: MainViewModel) {
            super.attachMainViewModel(mainViewModel)

            // initialize and connect list mode
            listMode = mainViewModel.uiState.mapState { it.listMode }
            viewModelScope.launch {
                repo.movies
                    .combine(listMode) { movies, listMode ->
                        filterMovies(listMode, movies)
                    }.collect { filteredMovies ->
                        _listedMovies.value = filteredMovies
                    }
            }
        }

        fun setListMode(listMode: ListMode) {
            mainViewModel.setListMode(listMode)
        }

        private fun filterMovies(
            listMode: ListMode,
            movies: List<Movie>,
        ): AsyncMovieInfo =
            AsyncMovieInfo.fromList(
                when (listMode) {
                    ListMode.ALL -> movies
                    ListMode.WATCHED -> movies.filter { it.watchState == WatchState.WATCHED }
                    ListMode.PENDING -> movies.filter { it.watchState == WatchState.PENDING }
                },
            )
    }
