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
        fun getMovie(movieId: Long): StateFlow<AsyncMovieInfo> = repo.singleMovie(movieId)

        fun updateMovieWatched(
            movieId: Long,
            watchState: WatchState,
        ) = viewModelScope.launch { repo.setWatchState(movieId, watchState) }
    }
