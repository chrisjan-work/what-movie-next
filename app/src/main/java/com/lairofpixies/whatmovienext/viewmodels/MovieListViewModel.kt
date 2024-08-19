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
import com.lairofpixies.whatmovienext.models.data.Preset
import com.lairofpixies.whatmovienext.models.data.hasMovie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.database.PresetRepository
import com.lairofpixies.whatmovienext.viewmodels.processors.FilterProcessor
import com.lairofpixies.whatmovienext.viewmodels.processors.SortProcessor
import com.lairofpixies.whatmovienext.views.state.BottomMenu
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.SortingSetup
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
        private val movieRepository: MovieRepository,
        private val presetRepository: PresetRepository,
        private val sortProcessor: SortProcessor,
        private val filterProcessor: FilterProcessor,
    ) : ScreenViewModel() {
        lateinit var listedMovies: StateFlow<AsyncMovie>
            private set

        private val _hasArchivedMovies = MutableStateFlow(false)
        val hasArchivedMovies: StateFlow<Boolean> = _hasArchivedMovies.asStateFlow()

        private val _bottomMenu = MutableStateFlow(BottomMenu.None)
        val bottomMenu: StateFlow<BottomMenu> = _bottomMenu.asStateFlow()

        private val _currentPreset = MutableStateFlow(Preset.Default)
        val currentPreset: StateFlow<Preset> = _currentPreset.asStateFlow()

        init {
            connectArchivedMovies()
            connectPresets()
        }

        private fun connectArchivedMovies() {
            viewModelScope.launch {
                movieRepository.archivedMovies.collect { movieInfo ->
                    _hasArchivedMovies.value = movieInfo.hasMovie()
                }
            }
        }

        private fun connectPresets() {
            viewModelScope.launch {
                presetRepository
                    .getPreset(Preset.FIXED_ID)
                    .collect { presetOrNull ->
                        _currentPreset.value = presetOrNull ?: Preset.Default
                    }
            }
        }

        override fun attachMainViewModel(mainViewModel: MainViewModel) {
            if (mainViewModel != this.mainViewModel) {
                super.attachMainViewModel(mainViewModel)

                listedMovies = mainViewModel.listedMovies

                viewModelScope.launch {
                    movieRepository.listedMovies
                        .combine(currentPreset) { movieInfo, preset ->
                            val filteredMovies =
                                filterProcessor.filterMovies(movieInfo, preset.listFilters)
                            sortProcessor.sortMovies(filteredMovies, preset.sortingSetup)
                        }.collect { sortedMovies ->
                            mainViewModel.updateMovies(sortedMovies)
                        }
                }
            }
        }

        fun setListFilters(listFilters: ListFilters) {
            viewModelScope.launch {
                presetRepository.updatePreset(
                    currentPreset.value.copy(listFilters = listFilters),
                )
            }
        }

        fun updateSortingSetup(sortingSetup: SortingSetup) {
            viewModelScope.launch {
                presetRepository.updatePreset(
                    currentPreset.value.copy(sortingSetup = sortingSetup),
                )
            }
        }

        fun onOpenSortingMenu() {
            viewModelScope.launch {
                _bottomMenu.value = BottomMenu.Sorting
            }
        }

        fun closeBottomMenu() {
            viewModelScope.launch {
                _bottomMenu.value = BottomMenu.None
            }
        }
    }
