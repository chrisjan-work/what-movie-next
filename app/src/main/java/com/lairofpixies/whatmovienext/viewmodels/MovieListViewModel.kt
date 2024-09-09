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
import com.lairofpixies.whatmovienext.models.data.Departments
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.Preset
import com.lairofpixies.whatmovienext.models.data.hasMovie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.database.PresetRepository
import com.lairofpixies.whatmovienext.models.mappers.PresetMapper
import com.lairofpixies.whatmovienext.util.quickMatchAll
import com.lairofpixies.whatmovienext.util.quickMatchAny
import com.lairofpixies.whatmovienext.viewmodels.processors.FilterProcessor
import com.lairofpixies.whatmovienext.viewmodels.processors.SortProcessor
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.BottomMenuOption
import com.lairofpixies.whatmovienext.views.state.BottomMenuState
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.QuickFind
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        private val presetMapper: PresetMapper,
    ) : ScreenViewModel() {
        lateinit var listedMovies: StateFlow<AsyncMovie>
            private set

        private val _databaseMovieCount = MutableStateFlow(0)
        val databaseMovieCount: StateFlow<Int> = _databaseMovieCount.asStateFlow()

        val isEmpty: Flow<Boolean> = movieRepository.isEmpty

        private val _hasArchivedMovies = MutableStateFlow(false)
        val hasArchivedMovies: StateFlow<Boolean> = _hasArchivedMovies.asStateFlow()

        private val _bottomMenuState =
            MutableStateFlow(BottomMenuState(BottomMenuOption.Sorting, false))
        val bottomMenuState: StateFlow<BottomMenuState> = _bottomMenuState.asStateFlow()

        private val _currentPreset = MutableStateFlow(Preset.Default)
        val currentPreset: StateFlow<Preset> = _currentPreset.asStateFlow()

        private val _allGenres = MutableStateFlow(emptyList<String>())
        val allGenres: StateFlow<List<String>> = _allGenres.asStateFlow()

        private val _allDirectors = MutableStateFlow(emptyList<String>())
        val allDirectors: StateFlow<List<String>> = _allDirectors.asStateFlow()

        private val _quickFind = MutableStateFlow(QuickFind.Default)
        val quickFind: StateFlow<QuickFind> = _quickFind.asStateFlow()

        private val _quickFindOpenAction = MutableSharedFlow<Boolean>()
        val quickFindOpenAction = _quickFindOpenAction.asSharedFlow()

        private val _canSpinRoulette = MutableStateFlow(false)
        val canSpinRoulette = _canSpinRoulette.asStateFlow()

        private val _dropdownShown = MutableStateFlow(false)
        val dropdownShown = _dropdownShown.asStateFlow()

        init {
            connectArchivedMovies()
            connectPresets()
            connectGenres()
            connectDirectors()
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

        private fun connectGenres() {
            viewModelScope.launch {
                movieRepository
                    .getAllGenresFromMovies()
                    .collect { genres ->
                        _allGenres.value = genres.sorted()
                    }
            }
        }

        private fun connectDirectors() {
            viewModelScope.launch {
                movieRepository
                    .getAllPeopleNamesByDepartment(Departments.Directors)
                    .collect { directors ->
                        // sort by family name (assumption: it's the last of the space-separated words)
                        _allDirectors.value = directors.sortedBy { it.split(" ").last() }
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
                            updateQuickFindText(quickFind.value.query)
                        }
                }

                viewModelScope.launch {
                    movieRepository.listedMovies.collect { allMovies ->
                        _databaseMovieCount.value = allMovies.toList<Movie.ForList>().size
                    }
                }

                viewModelScope.launch {
                    listedMovies.collect { movies ->
                        _canSpinRoulette.value = movies.hasMovie()
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

        fun onOpenBottomMenu(option: BottomMenuOption?) {
            viewModelScope.launch {
                _bottomMenuState.value =
                    BottomMenuState(
                        bottomMenuOption = option ?: bottomMenuState.value.bottomMenuOption,
                        isOpen = true,
                    )
            }
        }

        fun closeBottomMenu() {
            viewModelScope.launch {
                _bottomMenuState.value = bottomMenuState.value.copy(isOpen = false)
            }
        }

        fun onNavigateToRandomMovie() {
            val mainViewModel = mainViewModel ?: return
            if (canSpinRoulette.value) {
                val movieList =
                    mainViewModel.listedMovies.value
                        .toList<Movie.ForList>()
                val movieIndex = randomizer.nextInt(movieList.size)
                val movie = movieList.getOrNull(movieIndex) ?: return
                onNavigateWithParam(Routes.SingleMovieView, movie.appData.movieId, popToHome = true)
            }
        }

        fun presetMapper() = presetMapper

        fun updateQuickFindText(query: String) {
            viewModelScope.launch {
                if (query.isNotBlank()) {
                    // get a list of indices of the matched movies
                    val movieList =
                        mainViewModel?.listedMovies?.value?.toList<Movie.ForList>() ?: emptyList()
                    val matches =
                        movieList
                            .withIndex()
                            .filter { indexedMovie ->
                                val movie = indexedMovie.value
                                quickMatchAll(query, movie.searchData.title) ||
                                    movie.searchData.genres.any { quickMatchAny(query, it) } ||
                                    movie.detailData.directorNames.any { quickMatchAll(query, it) }
                            }.map { it.index }

                    // if a movie was already matched, update matchIndex
                    val matchIndex =
                        quickFind.value.movieIndex?.let {
                            matches.indexOf(it).takeIf { matchIndex -> matchIndex != -1 }
                        } ?: 0

                    _quickFind.value = QuickFind(query, matchIndex, matches)
                } else {
                    _quickFind.value = QuickFind.Default
                }
            }
        }

        fun jumpToNextQuickFind() {
            viewModelScope.launch {
                with(quickFind.value) {
                    if (matches.isNotEmpty()) {
                        _quickFind.value = copy(matchIndex = (matchIndex + 1) % matches.size)
                    }
                }
            }
        }

        fun onOpenQuickFind() {
            viewModelScope.launch {
                _quickFindOpenAction.emit(true)
            }
        }

        fun setDropdownShown(shown: Boolean) {
            viewModelScope.launch {
                _dropdownShown.value = shown
            }
        }
    }
