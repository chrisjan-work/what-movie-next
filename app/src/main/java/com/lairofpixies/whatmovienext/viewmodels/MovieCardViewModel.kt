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
import com.lairofpixies.whatmovienext.BuildConfig
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.navigation.Routes
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

        fun updateMovieWatchDates(
            movieId: Long,
            watchDates: List<Long>,
        ) = viewModelScope.launch { repo.updateWatchDates(movieId, watchDates) }

        fun archiveCurrentMovie() =
            viewModelScope.launch {
                val movieId =
                    currentMovie.value
                        .singleMovieOrNull<Movie.ForCard>()
                        ?.appData
                        ?.movieId ?: return@launch
                repo.archiveMovie(movieId)
            }

        fun canSpinRoulette(): Boolean =
            (mainViewModel?.listedMovies?.value as? AsyncMovie.Multiple)
                ?.movies
                ?.size
                ?.let { movieCount ->
                    movieCount > 1
                } ?: false

        fun onNavigateToRandomMovie(tabooId: Long) {
            val mainViewModel = mainViewModel ?: return
            if (canSpinRoulette()) {
                val movieList =
                    mainViewModel.listedMovies.value
                        .toList<Movie.ForList>()
                        .filter { it.appData.movieId != tabooId }
                val movieIndex = randomizer.nextInt(movieList.size)
                val movie = movieList.getOrNull(movieIndex) ?: return
                onNavigateWithParam(Routes.SingleMovieView, movie.appData.movieId, popToHome = true)
            }
        }

        fun canShare(): Boolean = currentMovie.value is AsyncMovie.Single

        fun shareableLink(): String =
            (currentMovie.value as? AsyncMovie.Single)
                ?.movie
                ?.searchData
                ?.tmdbId
                ?.let { id ->
                    "${BuildConfig.SHARE_SCHEME}://${BuildConfig.SHARE_HOST}/$id"
                } ?: ""
    }
