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

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.Preset
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.models.data.TestPreset.forApp
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.database.PresetRepository
import com.lairofpixies.whatmovienext.models.mappers.PresetMapper
import com.lairofpixies.whatmovienext.viewmodels.processors.FilterProcessor
import com.lairofpixies.whatmovienext.viewmodels.processors.SortProcessor
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.BottomMenuOption
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {
    private lateinit var listViewModel: MovieListViewModel
    private lateinit var mainViewModelMock: MainViewModel
    private lateinit var navHostControllerMock: NavHostController
    private lateinit var sortProcessor: SortProcessor
    private lateinit var filterProcessor: FilterProcessor
    private lateinit var presetMapper: PresetMapper
    private lateinit var movieRepository: MovieRepository
    private lateinit var presetRepository: PresetRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        movieRepository = mockk(relaxed = true)
        presetRepository = mockk(relaxed = true)

        mainViewModelMock = mockk(relaxed = true)
        navHostControllerMock = mockk(relaxed = true)
        sortProcessor = SortProcessor(mockk(relaxed = true))
        filterProcessor = FilterProcessor()
        presetMapper = mockk()
    }

    private fun construct() {
        listViewModel =
            MovieListViewModel(
                movieRepository,
                presetRepository,
                sortProcessor,
                filterProcessor,
                presetMapper,
            )
        listViewModel.attachMainViewModel(mainViewModelMock)
        listViewModel.attachNavHostController(navHostControllerMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun packMoviesToFlow(vararg movies: Movie.ForList) = flowOf(AsyncMovie.fromList(movies.toList()))

    @Test
    fun `movie list is processed during construction`() =
        runTest {
            // Given
            val seenMovie =
                forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
            val unseenMovie =
                forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
            every { movieRepository.listedMovies } returns
                packMoviesToFlow(unseenMovie, seenMovie)
            val asAsync = AsyncMovie.Multiple(listOf(unseenMovie, seenMovie))

            // When
            construct()

            // Then
            verify { mainViewModelMock.updateMovies(asAsync) }
        }

    @Test
    fun `forward list mode`() =
        runTest {
            // Given
            every { presetRepository.getPreset(any()) } returns
                flowOf(forApp().copy(listFilters = ListFilters(ListMode.WATCHED)))

            // When
            construct()

            // Then
            assertEquals(ListMode.WATCHED, listViewModel.currentPreset.value.listFilters.listMode)
        }

    @Test
    fun `change list mode`() =
        runTest {
            // Given
            val preset = slot<Preset>()
            coEvery { presetRepository.updatePreset(capture(preset)) } returns 1

            // When
            construct()
            listViewModel.setListFilters(ListFilters(ListMode.WATCHED))

            // Then
            assertEquals(ListMode.WATCHED, preset.captured.listFilters.listMode)
        }

    @Test
    fun `forward sort criteria`() =
        runTest {
            // Given
            val sortingSetup =
                SortingSetup(
                    SortingCriteria.MeanRating,
                    SortingDirection.Descending,
                )
            every { presetRepository.getPreset(any()) } returns
                flowOf(forApp().copy(sortingSetup = sortingSetup))

            // When
            construct()

            // Then
            assertEquals(sortingSetup, listViewModel.currentPreset.value.sortingSetup)
        }

    @Test
    fun `change sort criteria`() =
        runTest {
            // Given
            val sortingSetup =
                SortingSetup(
                    SortingCriteria.MeanRating,
                    SortingDirection.Descending,
                )
            val preset = slot<Preset>()
            coEvery { presetRepository.updatePreset(capture(preset)) } returns 1

            // When
            construct()
            listViewModel.updateSortingSetup(sortingSetup)

            // Then
            assertEquals(SortingCriteria.MeanRating, preset.captured.sortingSetup.criteria)
            assertEquals(SortingDirection.Descending, preset.captured.sortingSetup.direction)
        }

    @Test
    fun `detect if the archive is empty`() {
        // Given
        every { movieRepository.archivedMovies } returns
            packMoviesToFlow()

        // When
        construct()

        // Then
        val hasArchives = listViewModel.hasArchivedMovies.value
        assertEquals(false, hasArchives)
    }

    @Test
    fun `detect if there are archived movies`() {
        // Given
        every { movieRepository.archivedMovies } returns
            packMoviesToFlow(forList(title = "archived movie", isArchived = true))

        // When
        construct()

        // Then
        val hasArchives = listViewModel.hasArchivedMovies.value
        assertEquals(true, hasArchives)
    }

    @Test
    fun `control bottom menu visibility`() =
        runTest {
            construct()
            listViewModel.onOpenBottomMenu(BottomMenuOption.Sorting)
            assertEquals(
                BottomMenuOption.Sorting,
                listViewModel.bottomMenuState.value.bottomMenuOption,
            )
            assertEquals(true, listViewModel.bottomMenuState.value.isOpen)

            listViewModel.onOpenBottomMenu(BottomMenuOption.Filtering)
            assertEquals(
                BottomMenuOption.Filtering,
                listViewModel.bottomMenuState.value.bottomMenuOption,
            )
            assertEquals(true, listViewModel.bottomMenuState.value.isOpen)

            listViewModel.closeBottomMenu()
            assertEquals(false, listViewModel.bottomMenuState.value.isOpen)
        }

    @Test
    fun `showing menu without specifying tab reopens last tab`() =
        runTest {
            construct()
            listOf(BottomMenuOption.Sorting, BottomMenuOption.Filtering).forEach { option ->
                listViewModel.onOpenBottomMenu(option)
                listViewModel.closeBottomMenu()
                listViewModel.onOpenBottomMenu(null)
                assertEquals(option, listViewModel.bottomMenuState.value.bottomMenuOption)
            }
        }

    @Test
    fun `roulette feature`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Multiple(
                        listOf(
                            forList(id = 1, title = "Riddick"),
                            forList(id = 2, title = "Pitch Black"),
                            forList(id = 3, title = "The Chronicles of Riddick"),
                        ),
                    ),
                )
            construct()
            listViewModel.randomizer = Random(100)
            assertEquals(true, listViewModel.canSpinRoulette())

            // randomizer with fixed seed will produce fixed sequence
            val expectedSequence: List<Long> = listOf(3, 3, 3, 2, 1, 2)

            expectedSequence.forEach { expectedId ->
                clearMocks(navHostControllerMock)
                // When
                listViewModel.onNavigateToRandomMovie()

                // Then
                verify {
                    navHostControllerMock.navigate(
                        Routes.SingleMovieView.route(expectedId),
                        any<NavOptionsBuilder.() -> Unit>(),
                    )
                }
            }
        }

    @Test
    fun `roulette disabled with empty list`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(AsyncMovie.Empty)
            construct()
            assertEquals(false, listViewModel.canSpinRoulette())
        }

    @Test
    fun `forward preset mapper`() {
        construct()
        assertEquals(presetMapper, listViewModel.presetMapper())
    }
}
