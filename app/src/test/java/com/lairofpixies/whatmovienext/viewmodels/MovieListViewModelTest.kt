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
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.viewmodels.processors.SortProcessor
import com.lairofpixies.whatmovienext.views.state.BottomMenu
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import io.mockk.every
import io.mockk.mockk
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
    private lateinit var repo: MovieRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)

        mainViewModelMock = mockk(relaxed = true)
        every { mainViewModelMock.listFilters } returns
            MutableStateFlow(
                ListFilters(
                    listMode = ListMode.ALL,
                ),
            )

        navHostControllerMock = mockk(relaxed = true)
        sortProcessor = SortProcessor(Random(100))
    }

    private fun construct() {
        listViewModel = MovieListViewModel(repo, sortProcessor)
        listViewModel.attachMainViewModel(mainViewModelMock)
        listViewModel.attachNavHostController(navHostControllerMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun packMoviesToFlow(vararg movies: Movie.ForList) = flowOf(AsyncMovie.fromList(movies.toList()))

    @Test
    fun `update movie list with all movies filter`() =
        runTest {
            // Given
            val seenMovie =
                forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
            val unseenMovie =
                forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
            every { repo.listedMovies } returns
                packMoviesToFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.listFilters } returns
                MutableStateFlow(
                    ListFilters(
                        listMode = ListMode.ALL,
                    ),
                )

            // When
            construct()

            // Then
            verify {
                mainViewModelMock.updateMovies(
                    AsyncMovie.Multiple(
                        listOf(
                            unseenMovie,
                            seenMovie,
                        ),
                    ),
                )
            }
        }

    @Test
    fun `update movie list with only unseen movies`() =
        runTest {
            // Given
            val seenMovie =
                forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
            val unseenMovie =
                forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
            every { repo.listedMovies } returns
                packMoviesToFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.listFilters } returns
                MutableStateFlow(
                    ListFilters(
                        listMode = ListMode.PENDING,
                    ),
                )

            // When
            construct()

            // Then
            verify { mainViewModelMock.updateMovies(AsyncMovie.Single(unseenMovie)) }
        }

    @Test
    fun `update movie list with only seen movies`() =
        runTest {
            // Given
            val seenMovie =
                forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
            val unseenMovie =
                forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
            every { repo.listedMovies } returns
                packMoviesToFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.listFilters } returns
                MutableStateFlow(
                    ListFilters(
                        listMode = ListMode.WATCHED,
                    ),
                )

            // When
            construct()

            // Then
            verify { mainViewModelMock.updateMovies(AsyncMovie.Single(seenMovie)) }
        }

    @Test
    fun `forward list mode`() =
        runTest {
            // Given
            every { mainViewModelMock.listFilters } returns
                MutableStateFlow(
                    ListFilters(
                        listMode = ListMode.WATCHED,
                    ),
                )

            // When
            construct()

            // Then
            assertEquals(ListMode.WATCHED, listViewModel.listFilters.value.listMode)
        }

    @Test
    fun `change list mode`() =
        runTest {
            construct()
            listViewModel.setListFilters(ListFilters(ListMode.WATCHED))

            verify {
                mainViewModelMock.setListFilters(ListFilters(ListMode.WATCHED))
            }
        }

    @Test
    fun `detect if the archive is empty`() {
        // Given
        every { repo.archivedMovies } returns
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
        every { repo.archivedMovies } returns
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
            listViewModel.onOpenSortingMenu()
            assertEquals(BottomMenu.Sorting, listViewModel.bottomMenu.value)

            listViewModel.closeBottomMenu()
            assertEquals(BottomMenu.None, listViewModel.bottomMenu.value)

            val sortingSetup = SortingSetup(SortingCriteria.Genre, SortingDirection.Descending)
            listViewModel.updateSortingSetup(sortingSetup)
            assertEquals(sortingSetup, listViewModel.sortingSetup.value)
        }
}
