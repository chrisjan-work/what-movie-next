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

import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieCardViewModel
    @Inject
    constructor(
        private val repo: MovieRepository,
    ) : ScreenViewModel() {
        private val _currentMovie = MutableStateFlow<AsyncMovie>(AsyncMovie.Loading)
        val currentMovie = _currentMovie.asStateFlow()

        fun startFetchingMovie(movieId: Long?) {
            if (movieId == null) {
                _currentMovie.value = AsyncMovie.Empty
                return
            } else {
                _currentMovie.value = AsyncMovie.Loading
            }

            viewModelScope.launch {
                repo.singleCardMovie(movieId).collect {
                    _currentMovie.value = it
                }
            }
        }

        fun updateMovieWatched(
            movieId: Long,
            watchState: WatchState,
        ) = viewModelScope.launch { repo.setWatchState(movieId, watchState) }

        fun archiveCurrentMovie() =
            viewModelScope.launch {
                val movieId =
                    currentMovie.value
                        .singleMovieOrNull<Movie.ForCard>()
                        ?.appData
                        ?.id ?: return@launch
                repo.archiveMovie(movieId)
            }
    }
