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
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SharedMovieViewModel
    @Inject
    constructor(
        private val movieRepository: MovieRepository,
        private val apiRepository: ApiRepository,
    ) : ScreenViewModel() {
        private val _foundMovie = MutableStateFlow<AsyncMovie>(AsyncMovie.Empty)
        val foundMovie = _foundMovie.asStateFlow()

        private var searchJob: Job? = null

        private fun updateBusyDisplay(isBusy: Boolean) {
            if (isBusy) {
                showPopup(PopupInfo.Searching { cancelSearch() })
            } else {
                closePopupOfType(PopupInfo.Searching::class)
            }
        }

        private fun cancelSearch() {
            searchJob?.cancel()
            searchJob = null
            updateBusyDisplay(false)
            onLeaveAction()
        }

        fun fetchFromRemote(tmdbId: Long) {
            searchJob =
                viewModelScope.launch {
                    apiRepository.getMovieDetails(tmdbId).collect { asyncMovie ->
                        when (asyncMovie) {
                            is AsyncMovie.Loading -> {
                                updateBusyDisplay(true)
                            }

                            is AsyncMovie.Single -> {
                                _foundMovie.value = asyncMovie
                                updateBusyDisplay(false)
                            }

                            else -> {
                                failAndLeave()
                            }
                        }
                    }
                }
        }

        fun failAndLeave() {
            updateBusyDisplay(false)
            onLeaveAction()
            showPopup(PopupInfo.MovieNotFound)
        }

        fun onSaveAction() {
            viewModelScope.launch {
                val movieToSave = foundMovie.value.singleMovieOrNull<Movie.ForCard>()
                if (movieToSave == null || movieToSave.searchData.tmdbId <= 0) {
                    Timber.e("Attempting to save empty movie")
                    return@launch
                }
                movieRepository.storeMovie(movieToSave).join()
                onLeaveAction()
            }
        }
    }
