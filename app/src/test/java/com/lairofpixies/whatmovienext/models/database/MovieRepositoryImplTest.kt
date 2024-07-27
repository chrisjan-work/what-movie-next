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
package com.lairofpixies.whatmovienext.models.database

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryImplTest {
    private lateinit var movieDao: MovieDao
    private lateinit var sut: MovieRepository

    @Before
    fun setUp() {
        movieDao = mockk(relaxed = true)
    }

    @Test
    fun getMovies() =
        runTest {
            // Given
            val movies =
                listOf(
                    Movie(1, "first", WatchState.WATCHED),
                    Movie(2, "second", WatchState.WATCHED),
                )
            coEvery { movieDao.getAllMovies() } returns flowOf(movies)

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            val result = sut.movies.first()

            // Then
            assertEquals(AsyncMovieInfo.Multiple(movies), result)
        }

    @Test
    fun `single movie`() {
        // Given
        val movie = Movie(1, "first", WatchState.WATCHED)
        coEvery { movieDao.getMovie(1) } returns flowOf(movie)

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        val result = sut.singleMovie(1).value

        // Then
        assertEquals(AsyncMovieInfo.Single(movie), result)
    }

    @Test
    fun `single movie, loading`() {
        // Given
        coEvery { movieDao.getMovie(1) } returns emptyFlow()

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        val result = sut.singleMovie(1).value

        // Then
        assertEquals(AsyncMovieInfo.Loading, result)
    }

    @Test
    fun `single movie, not found`() {
        // Given
        coEvery { movieDao.getMovie(1) } returns flowOf(null)

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        val result = sut.singleMovie(1).value

        // Then
        assertEquals(AsyncMovieInfo.Empty, result)
    }

    @Test
    fun fetchMovieById() =
        runTest {
            // Given
            val movie = Movie(7, "gotById", WatchState.WATCHED)
            coEvery { movieDao.fetchMovieById(7) } returns movie

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            val result = sut.fetchMovieById(7)

            // Then
            assertEquals(movie, result)
        }

    @Test
    fun `fetch movie by id always returns null for 0`() =
        runTest {
            // Given
            coEvery { movieDao.fetchMovieById(0) } returns mockk()

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            val result = sut.fetchMovieById(0)

            // Then
            assertEquals(null, result)
        }

    @Test
    fun fetchMoviesByTitle() =
        runTest {
            // Given
            val movie = Movie(12, "gotByTitle", WatchState.WATCHED)
            coEvery { movieDao.fetchMoviesByTitle("gotByTitle") } returns listOf(movie)

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            val result = sut.fetchMoviesByTitle("gotByTitle")

            // Then
            assertEquals(listOf(movie), result)
        }

    @Test
    fun addMovie() =
        runTest {
            // Given
            val movie = slot<Movie>()
            coEvery { movieDao.insertMovie(capture(movie)) } returns 1L

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            sut.addMovie(Movie(title = "first"))

            // Then
            coVerify { movieDao.insertMovie(any()) }
            assertEquals(
                "first",
                movie.captured.title,
            )
        }

    @Test
    fun updateMovie() =
        runTest {
            // Given
            val movie = slot<Movie>()
            coEvery { movieDao.updateMovie(capture(movie)) } just runs

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            val updatedId = sut.updateMovie(Movie(id = 11, title = "first"))

            // Then
            coVerify { movieDao.updateMovie(any()) }
            assertEquals(
                "first",
                movie.captured.title,
            )
            assertEquals(11, updatedId)
        }

    @Test
    fun setWatchState() =
        runTest {
            // Given
            coEvery { movieDao.updateWatchState(any(), any()) } just runs

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            sut.setWatchState(11, WatchState.WATCHED)

            // Then
            coVerify { movieDao.updateWatchState(11, WatchState.WATCHED) }
        }

    @Test
    fun archiveMovie() =
        runTest {
            // Given
            val movieToArchive = Movie(1, "toArchive", WatchState.WATCHED, isArchived = false)
            val requestedMovie = slot<Movie>()
            coEvery { movieDao.delete(capture(requestedMovie)) } just runs

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            sut.archiveMovie(movieToArchive.id)

            // Then
            coVerify { movieDao.archive(movieToArchive.id) }
        }

    @Test
    fun restoreMovie() =
        runTest {
            // Given
            val movieToRestore = Movie(1, "isArchived", WatchState.WATCHED, isArchived = true)
            val requestedMovieId = slot<Long>()
            coEvery { movieDao.restore(capture(requestedMovieId)) } just runs

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            sut.restoreMovie(movieToRestore.id)

            // Then
            coVerify { movieDao.restore(movieToRestore.id) }
        }

    @Test
    fun deleteMovie() =
        runTest {
            // Given
            val movieToDelete = Movie(1, "isArchived", WatchState.WATCHED, isArchived = true)
            val archivedMovie = slot<Movie>()
            coEvery { movieDao.delete(capture(archivedMovie)) } just runs

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            sut.deleteMovie(movieToDelete)

            // Then
            coVerify { movieDao.delete(movieToDelete) }
        }
}
