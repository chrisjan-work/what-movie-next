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
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.BottomMenu
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MovieListDisplayState
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
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {
    private lateinit var listViewModel: MovieListViewModel
    private lateinit var mainViewModelMock: MainViewModel
    private lateinit var repo: MovieRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)

        mainViewModelMock = mockk(relaxed = true)
        every { mainViewModelMock.movieListDisplayState } returns
            MutableStateFlow(
                MovieListDisplayState(
                    listMode = ListMode.ALL,
                ),
            )
    }

    private fun construct() {
        listViewModel = MovieListViewModel(repo)
        listViewModel.attachMainViewModel(mainViewModelMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun packMoviesToFlow(vararg movies: Movie.ForList) = flowOf(AsyncMovie.fromList(movies.toList()))

    @Test
    fun `forward movie list with all movies filter`() =
        runTest {
            // Given
            val seenMovie =
                forList(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                forList(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.listedMovies } returns
                packMoviesToFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.movieListDisplayState } returns
                MutableStateFlow(
                    MovieListDisplayState(
                        listMode = ListMode.ALL,
                    ),
                )

            // When
            construct()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovie.Multiple(listOf(unseenMovie, seenMovie)), forwardedMovies)
        }

    @Test
    fun `forward movie list with only unseen movies`() =
        runTest {
            // Given
            val seenMovie =
                forList(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                forList(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.listedMovies } returns
                packMoviesToFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.movieListDisplayState } returns
                MutableStateFlow(
                    MovieListDisplayState(
                        listMode = ListMode.PENDING,
                    ),
                )

            // When
            construct()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovie.Single(unseenMovie), forwardedMovies)
        }

    @Test
    fun `forward movie list with only seen movies`() =
        runTest {
            // Given
            val seenMovie =
                forList(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                forList(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.listedMovies } returns
                packMoviesToFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.movieListDisplayState } returns
                MutableStateFlow(
                    MovieListDisplayState(
                        listMode = ListMode.WATCHED,
                    ),
                )

            // When
            construct()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovie.Single(seenMovie), forwardedMovies)
        }

    @Test
    fun `forward list mode`() =
        runTest {
            // Given
            every { mainViewModelMock.movieListDisplayState } returns
                MutableStateFlow(
                    MovieListDisplayState(
                        listMode = ListMode.WATCHED,
                    ),
                )

            // When
            construct()

            // Then
            assertEquals(ListMode.WATCHED, listViewModel.listMode.value)
        }

    @Test
    fun `change list mode`() =
        runTest {
            construct()
            listViewModel.setListMode(ListMode.WATCHED)

            verify {
                mainViewModelMock.setListMode(ListMode.WATCHED)
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
    fun `sort movies by date`() {
        fun generateList(vararg date: Long) = date.map { forList(creationTime = it) }
        // Given
        val moviesToSort = generateList(200, 100, 400)

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.CreationTime, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.CreationTime, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(generateList(100, 200, 400), up.movies)
        assertEquals(generateList(400, 200, 100), down.movies)
    }

    @Test
    fun `sort movies by title`() {
        fun generateList(vararg title: String) = title.map { forList(title = it) }
        // Given
        val moviesToSort = generateList("BBBB", "CCCC", "AAAA")

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Title, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Title, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(generateList("AAAA", "BBBB", "CCCC"), up.movies)
        assertEquals(generateList("CCCC", "BBBB", "AAAA"), down.movies)
    }

    @Test
    fun `sort movies by year`() {
        fun generateList(vararg year: Int) = year.map { forList(year = it) }
        // Given
        val moviesToSort = generateList(2000, 2001, 1999)

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Year, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Year, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(generateList(1999, 2000, 2001), up.movies)
        assertEquals(generateList(2001, 2000, 1999), down.movies)
    }

    @Test
    fun `sort movies by watchcount`() {
        fun generateList(vararg watchState: WatchState) = watchState.map { forList(watchState = it) }
        // Given
        val moviesToSort = generateList(WatchState.WATCHED, WatchState.PENDING, WatchState.PENDING)

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.WatchCount, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.WatchCount, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(
            generateList(WatchState.PENDING, WatchState.PENDING, WatchState.WATCHED),
            up.movies,
        )
        assertEquals(
            generateList(WatchState.WATCHED, WatchState.PENDING, WatchState.PENDING),
            down.movies,
        )
    }

    @Test
    fun `sort movies by genre`() {
        fun generateList(vararg genres: String) = genres.map { forList(genres = it.split(",")) }
        // Given
        val moviesToSort = generateList("Comedy", "Action,Comedy", "Action,Drama")

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Genre, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Genre, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(generateList("Action,Comedy", "Action,Drama", "Comedy"), up.movies)
        assertEquals(generateList("Comedy", "Action,Drama", "Action,Comedy"), down.movies)
    }

    @Test
    fun `sort movies by runtime`() {
        fun generateList(vararg runtime: Int) = runtime.map { forList(runtimeMinutes = it) }
        // Given
        val moviesToSort = generateList(200, 201, 199)

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Runtime, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Runtime, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(generateList(199, 200, 201), up.movies)
        assertEquals(generateList(201, 200, 199), down.movies)
    }

    @Test
    fun `sort movies by director`() {
        fun generateList(vararg directors: String) = directors.map { forList(directors = it.split(",")) }
        // Given
        val moviesToSort = generateList("Almodovar", "Tarantino", "Kitano")

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Director, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Director, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(generateList("Almodovar", "Kitano", "Tarantino"), up.movies)
        assertEquals(generateList("Tarantino", "Kitano", "Almodovar"), down.movies)
    }

    @Test
    fun `sort movies by ratings`() {
        fun generateList(vararg ratings: Pair<Int, Int>) = ratings.map { forList(rtRating = it.first, mcRating = it.second) }
        // Given
        val r50 = 50 to 50 // average
        val r45 = 60 to 30 // average
        val r20 = 20 to -1 // take first
        val r25 = -1 to 25 // take second
        val r0 = -1 to -1 // take none
        val moviesToSort = generateList(r50, r45, r20, r25, r0)

        // When
        construct()
        val up =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.MeanRating, SortingDirection.Ascending),
            ) as AsyncMovie.Multiple
        val down =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.MeanRating, SortingDirection.Descending),
            ) as AsyncMovie.Multiple

        // Then
        assertEquals(generateList(r0, r20, r25, r45, r50), up.movies)
        assertEquals(generateList(r50, r45, r25, r20, r0), down.movies)
    }

    @Test
    fun `random sort`() {
        fun generateList(vararg title: String) = title.map { forList(title = it) }
        // Given
        val moviesToSort = generateList("five", "one", "three", "seven")

        // When
        construct()
        val randomized =
            listViewModel.sortMovies(
                AsyncMovie.fromList(moviesToSort),
                SortingSetup(SortingCriteria.Random, SortingDirection.Ascending),
                seed = 100,
            ) as AsyncMovie.Multiple

        // Then
        // equal sets -> contain the same elements
        assertEquals(moviesToSort.toSet(), randomized.movies.toSet())
        // different lists -> order is different
        assertNotEquals(moviesToSort, randomized.movies)
        assertNotEquals(moviesToSort.sortedBy { it.searchData.title }, randomized.movies)
        assertNotEquals(moviesToSort.sortedByDescending { it.searchData.title }, randomized.movies)
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
