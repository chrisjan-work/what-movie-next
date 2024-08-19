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
import com.lairofpixies.whatmovienext.models.data.hasMovie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.viewmodels.processors.SortProcessor
import com.lairofpixies.whatmovienext.views.state.BottomMenu
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
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
        private val repo: MovieRepository,
        private val sortProcessor: SortProcessor,
    ) : ScreenViewModel() {
        lateinit var listedMovies: StateFlow<AsyncMovie>
            private set

        private val _hasArchivedMovies = MutableStateFlow(false)
        val hasArchivedMovies: StateFlow<Boolean> = _hasArchivedMovies.asStateFlow()

        private val _bottomMenu = MutableStateFlow(BottomMenu.None)
        val bottomMenu: StateFlow<BottomMenu> =
            _bottomMenu.asStateFlow()

        private val _sortingSetup = MutableStateFlow(SortingSetup.Default)
        val sortingSetup: StateFlow<SortingSetup> = _sortingSetup.asStateFlow()

        lateinit var listFilters: StateFlow<ListFilters>
            private set

        init {
            viewModelScope.launch {
                repo.archivedMovies.collect { movieInfo ->
                    _hasArchivedMovies.value = movieInfo.hasMovie()
                }
            }
        }

        override fun attachMainViewModel(mainViewModel: MainViewModel) {
            if (mainViewModel != this.mainViewModel) {
                super.attachMainViewModel(mainViewModel)

                // initialize and connect main view model flows
                listedMovies = mainViewModel.listedMovies
                listFilters = mainViewModel.listFilters

                viewModelScope.launch {
                    repo.listedMovies
                        .combine(listFilters) { movieInfo, listFilters ->
                            filterMovies(movieInfo, listFilters.listMode)
                        }.combine(sortingSetup) { filteredMovies, sorting ->
                            sortProcessor.sortMovies(filteredMovies, sorting)
                        }.collect { sortedMovies ->
                            mainViewModel.updateMovies(sortedMovies)
                        }
                }
            }
        }

        fun setListFilters(listFilters: ListFilters) {
            mainViewModel?.setListFilters(listFilters)
        }

        private fun filterMovies(
            movies: AsyncMovie,
            listMode: ListMode,
        ): AsyncMovie =
            when (listMode) {
                ListMode.ALL -> movies
                ListMode.WATCHED -> movies.filter { it.appData?.watchDates?.isNotEmpty() == true }
                ListMode.PENDING -> movies.filter { it.appData?.watchDates?.isEmpty() == true }
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

        fun updateSortingSetup(setup: SortingSetup) {
            viewModelScope.launch {
                _sortingSetup.value = setup
            }
        }
    }
