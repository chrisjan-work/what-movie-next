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

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Departments
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
import com.lairofpixies.whatmovienext.views.state.QuickFind
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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

        mainViewModelMock =
            mockk(relaxed = true) {
                every { listedMovies } returns MutableStateFlow(AsyncMovie.Empty)
            }
        sortProcessor = SortProcessor(mockk(relaxed = true))
        filterProcessor = FilterProcessor()
        presetMapper = mockk(relaxed = true)
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
            assertEquals(true, listViewModel.canSpinRoulette.value)

            // randomizer with fixed seed will produce fixed sequence
            val expectedSequence: List<Long> = listOf(3, 3, 3, 2, 1, 2)

            expectedSequence.forEach { expectedId ->
                clearMocks(mainViewModelMock, answers = false, recordedCalls = true)
                // When
                listViewModel.onNavigateToRandomMovie()

                // Then
                verify {
                    mainViewModelMock.onNavigateWithParam(
                        Routes.SingleMovieView,
                        expectedId,
                        popToHome = true,
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
            assertEquals(false, listViewModel.canSpinRoulette.value)
        }

    @Test
    fun `forward preset mapper`() {
        construct()
        assertEquals(presetMapper, listViewModel.presetMapper())
    }

    @Test
    fun `expose genre list`() =
        runTest {
            // Given
            val genres = listOf("Action", "Comedy", "Drama")
            every { movieRepository.getAllGenresFromMovies() } returns
                flowOf(genres)

            // When
            construct()

            // Then
            assertEquals(genres, listViewModel.allGenres.value)
        }

    @Test
    fun `expose directors list`() =
        runTest {
            // Given
            val directors = listOf("Woody Allen", "Roman Polanski")
            every { movieRepository.getAllPeopleNamesByDepartment(Departments.Directors) } returns
                flowOf(directors)

            // When
            construct()

            // Then
            assertEquals(directors, listViewModel.allDirectors.value)
        }

    @Test
    fun `sort directors by family name`() =
        runTest {
            // Given
            val directors = listOf("Max Mustermann", "Peter Peterovitsch Bean", "Zack Aaron")
            every { movieRepository.getAllPeopleNamesByDepartment(Departments.Directors) } returns
                flowOf(directors)

            // When
            construct()

            // Then
            val sortedDirectors = listOf("Zack Aaron", "Peter Peterovitsch Bean", "Max Mustermann")
            assertEquals(sortedDirectors, listViewModel.allDirectors.value)
        }

    @Test
    fun `quickfind updates for title matching`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Multiple(
                        listOf(
                            forList(id = 0, title = "nope"),
                            forList(id = 1, title = "the abcd of literature"),
                            forList(id = 2, title = "skipme"),
                            forList(id = 3, title = "an abc"),
                            forList(id = 4, title = "tail"),
                        ),
                    ),
                )
            construct()

            // When
            listViewModel.updateQuickFindText("abc")
            assertEquals(
                QuickFind("abc", 0, listOf(1, 3)),
                listViewModel.quickFind.value,
            )

            listViewModel.updateQuickFindText("abcd")
            assertEquals(
                QuickFind("abcd", 0, listOf(1)),
                listViewModel.quickFind.value,
            )

            listViewModel.updateQuickFindText("an bc")
            assertEquals(
                QuickFind("an bc", 0, listOf(3)),
                listViewModel.quickFind.value,
            )

            listViewModel.updateQuickFindText("")
            assertEquals(
                QuickFind.Default,
                listViewModel.quickFind.value,
            )
        }

    @Test
    fun `quickfind updates for match details`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Multiple(
                        listOf(
                            forList(id = 0, title = "skipme"),
                            forList(id = 1, title = "bytitle"),
                            forList(
                                id = 2,
                                title = "bygenresingle",
                                genres = listOf("singlegenre"),
                            ),
                            forList(
                                id = 3,
                                title = "bygenreany",
                                genres = listOf("miss", "match"),
                            ),
                            forList(
                                id = 4,
                                title = "bydirector",
                                directors = listOf("jack", "jill"),
                            ),
                        ),
                    ),
                )
            construct()

            // When
            listViewModel.updateQuickFindText("bytitle")
            assertEquals(
                QuickFind("bytitle", 0, listOf(1)),
                listViewModel.quickFind.value,
            )

            listViewModel.updateQuickFindText("singlegenre")
            assertEquals(
                QuickFind("singlegenre", 0, listOf(2)),
                listViewModel.quickFind.value,
            )
            listViewModel.updateQuickFindText("another match")
            assertEquals(
                QuickFind("another match", 0, listOf(3)),
                listViewModel.quickFind.value,
            )

            listViewModel.updateQuickFindText("jack")
            assertEquals(
                QuickFind("jack", 0, listOf(4)),
                listViewModel.quickFind.value,
            )

            listViewModel.updateQuickFindText("jack jill")
            assertEquals(
                QuickFind("jack jill", 0, emptyList()),
                listViewModel.quickFind.value,
            )
        }

    @Test
    fun `quickfind next`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Multiple(
                        listOf(
                            forList(id = 0, title = "nope"),
                            forList(id = 1, title = "first match"),
                            forList(id = 2, title = "second match"),
                            forList(id = 3, title = "miss"),
                        ),
                    ),
                )
            construct()
            listViewModel.updateQuickFindText("match")
            assertEquals(
                QuickFind("match", 0, listOf(1, 2)),
                listViewModel.quickFind.value,
            )

            // When
            listViewModel.jumpToNextQuickFind()
            assertEquals(
                QuickFind("match", 1, listOf(1, 2)),
                listViewModel.quickFind.value,
            )

            listViewModel.jumpToNextQuickFind()
            assertEquals(
                QuickFind("match", 0, listOf(1, 2)),
                listViewModel.quickFind.value,
            )
        }

    @Test
    fun `quickfind stay selected`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Multiple(
                        listOf(
                            forList(id = 0, title = "nope"),
                            forList(id = 1, title = "first match"),
                            forList(id = 2, title = "middle catch"),
                            forList(id = 3, title = "second match"),
                        ),
                    ),
                )
            construct()
            listViewModel.updateQuickFindText("atch")
            listViewModel.jumpToNextQuickFind()
            listViewModel.jumpToNextQuickFind()
            assertEquals(
                QuickFind("atch", 2, listOf(1, 2, 3)),
                listViewModel.quickFind.value,
            )

            // When
            listViewModel.updateQuickFindText("match")
            assertEquals(
                QuickFind("match", 1, listOf(1, 3)),
                listViewModel.quickFind.value,
            )
        }

    @Test
    fun `forward open quickfind input`() =
        runTest {
            // Given
            construct()
            var clickCount = 0
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                listViewModel.quickFindOpenAction.collect { clickCount++ }
            }

            // When
            listViewModel.onOpenQuickFind()

            // Then
            assertEquals(1, clickCount)
        }

    @Test
    fun `dropdown menu`() =
        runTest {
            // Given
            construct()

            // When
            val defaultValue = listViewModel.dropdownShown.value
            listViewModel.setDropdownShown(true)
            val isTrue = listViewModel.dropdownShown.value
            listViewModel.setDropdownShown(false)
            val isFalse = listViewModel.dropdownShown.value

            // Then
            assertEquals(false, defaultValue)
            assertEquals(true, isTrue)
            assertEquals(false, isFalse)
        }

    @Test
    fun `trigger data export`() =
        runTest {
            construct()
            listViewModel.exportAllMovies()
            coVerify { mainViewModelMock.requestExport(any()) }
        }

    @Test
    fun `trigger data import`() =
        runTest {
            construct()
            listViewModel.importMovies()
            coVerify { mainViewModelMock.requestImport() }
        }
}
