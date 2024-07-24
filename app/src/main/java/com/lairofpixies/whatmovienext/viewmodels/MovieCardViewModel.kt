package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieCardViewModel
    @Inject
    constructor(
        private val repo: MovieRepository,
    ) : ScreenViewModel() {
        fun getMovie(movieId: Long): StateFlow<AsyncMovieInfo> = repo.getMovie(movieId)

        fun updateMovieWatched(
            movieId: Long,
            watchState: WatchState,
        ) = viewModelScope.launch { repo.setWatchState(movieId, watchState) }
    }
