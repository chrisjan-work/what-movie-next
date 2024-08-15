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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.data.hasMovie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.util.mapState
import com.lairofpixies.whatmovienext.views.state.BottomMenu
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MovieListViewModel
    @Inject
    constructor(
        private val repo: MovieRepository,
        private val randomizer: Random,
    ) : ScreenViewModel() {
        private val _listedMovies = MutableStateFlow<AsyncMovie>(AsyncMovie.Loading)
        val listedMovies: StateFlow<AsyncMovie> = _listedMovies.asStateFlow()

        private val _hasArchivedMovies = MutableStateFlow(false)
        val hasArchivedMovies: StateFlow<Boolean> = _hasArchivedMovies.asStateFlow()

        private val _bottomMenu = MutableStateFlow(BottomMenu.None)
        val bottomMenu: StateFlow<BottomMenu> =
            _bottomMenu.asStateFlow()

        private val _sortingSetup = MutableStateFlow(SortingSetup.Default)
        val sortingSetup: StateFlow<SortingSetup> = _sortingSetup.asStateFlow()

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
            if (mainViewModel != this.mainViewModel) {
                super.attachMainViewModel(mainViewModel)

                // initialize and connect list mode
                listMode = mainViewModel.movieListDisplayState.mapState { it.listMode }
                viewModelScope.launch {
                    repo.listedMovies
                        .combine(listMode) { movieInfo, listMode ->
                            filterMovies(movieInfo, listMode)
                        }.combine(sortingSetup) { filteredMovies, sorting ->
                            sortMovies(filteredMovies, sorting)
                        }.collect { sortedMovies ->
                            _listedMovies.value = sortedMovies
                        }
                }
            }
        }

        fun setListMode(listMode: ListMode) {
            mainViewModel?.setListMode(listMode)
        }

        private fun filterMovies(
            movies: AsyncMovie,
            listMode: ListMode,
        ): AsyncMovie =
            when (listMode) {
                ListMode.ALL -> movies
                ListMode.WATCHED -> movies.filter { it.appData?.watchState == WatchState.WATCHED }
                ListMode.PENDING -> movies.filter { it.appData?.watchState == WatchState.PENDING }
            }

        @VisibleForTesting
        fun sortMovies(
            movies: AsyncMovie,
            sortingSetup: SortingSetup,
        ): AsyncMovie {
            val unsorted = movies.toList<Movie.ForList>()
            val sortedAscending =
                when (sortingSetup.criteria) {
                    SortingCriteria.CreationTime ->
                        unsorted.sortedBy { it.appData.creationTime }

                    SortingCriteria.Title ->
                        unsorted.sortedBy { it.searchData.title }

                    SortingCriteria.Year ->
                        unsorted.sortedBy { it.searchData.year ?: 0 }

                    SortingCriteria.WatchCount ->
                        unsorted.sortedBy { it.appData.watchState }

                    SortingCriteria.Genre ->
                        unsorted.sortedBy { it.searchData.genres.joinToString(",") }

                    SortingCriteria.Runtime ->
                        unsorted.sortedBy { it.detailData.runtimeMinutes }

                    SortingCriteria.Director ->
                        unsorted.sortedBy {
                            it.detailData.directorNames.joinToString(",")
                        }

                    SortingCriteria.MeanRating ->
                        unsorted.sortedBy { movie ->
                            listOf(
                                movie.detailData.rtRating.percentValue,
                                movie.detailData.mcRating.percentValue,
                            ).filter { it >= 0 }
                                .average()
                                .takeIf { !it.isNaN() } ?: 0.0
                        }

                    SortingCriteria.Random -> {
                        val order = List(unsorted.size) { randomizer.nextDouble() }
                        unsorted.zip(order).sortedBy { it.second }.map { it.first }
                    }
                }

            val sortedList =
                if (sortingSetup.direction == SortingDirection.Descending) {
                    sortedAscending.reversed()
                } else {
                    sortedAscending
                }
            return AsyncMovie.fromList(sortedList)
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
