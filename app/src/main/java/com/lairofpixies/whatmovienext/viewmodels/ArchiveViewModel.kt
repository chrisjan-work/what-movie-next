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
import com.lairofpixies.whatmovienext.models.data.AMovie
import com.lairofpixies.whatmovienext.models.data.LoadingAMovie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
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
        private val _archivedMovies = MutableStateFlow<LoadingAMovie>(LoadingAMovie.Loading)
        val archivedMovies: StateFlow<LoadingAMovie> = _archivedMovies.asStateFlow()

        private val _selection = MutableStateFlow(emptySet<AMovie.ForList>())
        val selection: StateFlow<Set<AMovie.ForList>> = _selection.asStateFlow()

        init {
            viewModelScope.launch {
                repo.archivedMovies.collect { movieInfo ->
                    _archivedMovies.value = movieInfo
                }
            }
        }

        fun select(movie: AMovie.ForList) {
            _selection.value = selection.value + movie
        }

        fun deselect(movie: AMovie.ForList) {
            _selection.value = selection.value - movie
        }

        fun restoreSelectedMovies() =
            viewModelScope.launch {
                selection.value.forEach {
                    repo.restoreMovie(it.appData.id)
                }
                _selection.value = emptySet()
            }

        fun deleteSelectedMovies() {
            showPopup(
                PopupInfo.ConfirmDeletion {
                    viewModelScope.launch {
                        selection.value.forEach {
                            repo.deleteMovie(it.appData.id)
                        }
                    }
                    _selection.value = emptySet()
                },
            )
        }
    }
