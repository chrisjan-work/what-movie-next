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

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MovieListDisplayState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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

    private fun rerunConstructor() {
        listViewModel = MovieListViewModel(repo)
        listViewModel.attachMainViewModel(mainViewModelMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun packMoviesToStateFlow(vararg movies: Movie): StateFlow<AsyncMovieInfo> =
        MutableStateFlow(
            AsyncMovieInfo.fromList(movies.toList()),
        ).asStateFlow()

    @Test
    fun `forward movie list with all movies filter`() =
        runTest {
            // Given
            val seenMovie = Movie(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                Movie(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.movies } returns
                packMoviesToStateFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.movieListDisplayState } returns
                MutableStateFlow(
                    MovieListDisplayState(
                        listMode = ListMode.ALL,
                    ),
                )

            // When
            rerunConstructor()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovieInfo.Multiple(listOf(unseenMovie, seenMovie)), forwardedMovies)
        }

    @Test
    fun `forward movie list with only unseen movies`() =
        runTest {
            // Given
            val seenMovie = Movie(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                Movie(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.movies } returns
                packMoviesToStateFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.movieListDisplayState } returns
                MutableStateFlow(
                    MovieListDisplayState(
                        listMode = ListMode.PENDING,
                    ),
                )

            // When
            rerunConstructor()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovieInfo.Single(unseenMovie), forwardedMovies)
        }

    @Test
    fun `forward movie list with only seen movies`() =
        runTest {
            // Given
            val seenMovie = Movie(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                Movie(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.movies } returns
                packMoviesToStateFlow(unseenMovie, seenMovie)
            every { mainViewModelMock.movieListDisplayState } returns
                MutableStateFlow(
                    MovieListDisplayState(
                        listMode = ListMode.WATCHED,
                    ),
                )

            // When
            rerunConstructor()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovieInfo.Single(seenMovie), forwardedMovies)
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
            rerunConstructor()

            // Then
            assertEquals(ListMode.WATCHED, listViewModel.listMode.value)
        }

    @Test
    fun `detect if the archive is empty`() {
        // Given
        every { repo.archivedMovies } returns
            packMoviesToStateFlow()

        // When
        rerunConstructor()

        // Then
        val hasArchives = listViewModel.hasArchivedMovies.value
        assertEquals(false, hasArchives)
    }

    @Test
    fun `detect if there are archived movies`() {
        // Given
        every { repo.archivedMovies } returns
            packMoviesToStateFlow(Movie(title = "archived movie", isArchived = true))

        // When
        rerunConstructor()

        // Then
        val hasArchives = listViewModel.hasArchivedMovies.value
        assertEquals(true, hasArchives)
    }
}
