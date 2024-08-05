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
import com.lairofpixies.whatmovienext.models.data.LoadingMovie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.hasMovie
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
        private val _listedMovies = MutableStateFlow<LoadingMovie>(LoadingMovie.Loading)
        val listedMovies: StateFlow<LoadingMovie> = _listedMovies.asStateFlow()

        private val _hasArchivedMovies = MutableStateFlow(false)
        val hasArchivedMovies: StateFlow<Boolean> = _hasArchivedMovies.asStateFlow()

        lateinit var listMode: StateFlow<ListMode>
            private set

        init {
            viewModelScope.launch {
                repo.archivedMovies.collect { movieInfo ->
                    _hasArchivedMovies.value = movieInfo.hasMovie()
                }
            }
        }

        override fun attachMainViewModel(mainViewModel: MainViewModel) {
            super.attachMainViewModel(mainViewModel)

            // initialize and connect list mode
            listMode = mainViewModel.movieListDisplayState.mapState { it.listMode }
            viewModelScope.launch {
                repo.movies
                    .combine(listMode) { movieInfo, listMode ->
                        filterMovies(movieInfo, listMode)
                    }.collect { filteredMovies ->
                        _listedMovies.value = filteredMovies
                    }
            }
        }

        fun setListMode(listMode: ListMode) {
            mainViewModel.setListMode(listMode)
        }

        private fun filterMovies(
            movieInfo: LoadingMovie,
            listMode: ListMode,
        ): LoadingMovie =
            when (listMode) {
                ListMode.ALL -> movieInfo
                ListMode.WATCHED -> movieInfo.filter { it.watchState == WatchState.WATCHED }
                ListMode.PENDING -> movieInfo.filter { it.watchState == WatchState.PENDING }
            }
    }
