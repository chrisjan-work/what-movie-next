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

import com.lairofpixies.whatmovienext.models.data.LoadingMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.TestAMovie.forCard
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.models.mappers.DbMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryImplTest {
    private lateinit var movieDao: MovieDao
    private lateinit var dbMapper: DbMapper
    private lateinit var movieRepository: MovieRepository

    @Before
    fun setUp() {
        movieDao = mockk(relaxed = true)
        dbMapper = DbMapper()
    }

    private fun TestScope.initializeSut() {
        movieRepository =
            MovieRepositoryImpl(
                movieDao,
                dbMapper,
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            )
        advanceUntilIdle()
    }

    @Test
    fun getMovies() =
        runTest {
            // Given
            val dbMovies =
                listOf(
                    DbMovie(id = 1, title = "first", watchState = WatchState.WATCHED),
                    DbMovie(id = 2, title = "second", watchState = WatchState.WATCHED),
                )
            coEvery { movieDao.getAllMovies() } returns flowOf(dbMovies)

            // When
            initializeSut()
            val result = movieRepository.movies.last()

            // Then
            val loadingMovies =
                LoadingMovie.Multiple(
                    listOf(
                        Movie(id = 1, title = "first", watchState = WatchState.WATCHED),
                        Movie(id = 2, title = "second", watchState = WatchState.WATCHED),
                    ),
                )
            assertEquals(loadingMovies, result)
        }

    @Test
    fun getArchivedMovies() =
        runTest {
            // Given
            val dbMovies =
                listOf(
                    DbMovie(
                        id = 1,
                        title = "first",
                        watchState = WatchState.WATCHED,
                        isArchived = true,
                    ),
                    DbMovie(
                        id = 2,
                        title = "second",
                        watchState = WatchState.WATCHED,
                        isArchived = true,
                    ),
                )
            coEvery { movieDao.getArchivedMovies() } returns flowOf(dbMovies)

            // When
            initializeSut()
            val result = movieRepository.archivedMovies.last()

            // Then
            val loadingMovies =
                LoadingMovie.Multiple(
                    listOf(
                        Movie(
                            id = 1,
                            title = "first",
                            watchState = WatchState.WATCHED,
                            isArchived = true,
                        ),
                        Movie(
                            id = 2,
                            title = "second",
                            watchState = WatchState.WATCHED,
                            isArchived = true,
                        ),
                    ),
                )
            assertEquals(loadingMovies, result)
        }

    @Test
    fun `single movie`() =
        runTest {
            // Given
            coEvery { movieDao.getMovie(1) } returns
                flowOf(
                    DbMovie(
                        id = 1,
                        title = "first",
                        watchState = WatchState.WATCHED,
                    ),
                )

            // When
            initializeSut()
            val result = movieRepository.singleMovie(1).first()

            // Then
            assertEquals(
                LoadingMovie.Single(Movie(1, "first", watchState = WatchState.WATCHED)),
                result,
            )
        }

    @Test
    fun `single movie, not found`() =
        runTest {
            // Given
            coEvery { movieDao.getMovie(1) } returns flowOf(null)

            // When
            initializeSut()
            val result = movieRepository.singleMovie(1).first()

            // Then
            assertEquals(LoadingMovie.Empty, result)
        }

    @Test
    fun addMovie() =
        runTest {
            // Given
            val dbMovie = slot<DbMovie>()
            coEvery { movieDao.insertMovie(capture(dbMovie)) } returns 1L

            // When
            initializeSut()
            movieRepository.addMovie(Movie(title = "first"))

            // Then
            coVerify { movieDao.insertMovie(any()) }
            assertEquals(
                "first",
                dbMovie.captured.title,
            )
        }

    @Test
    fun updateMovie() =
        runTest {
            // Given
            val dbMovie = slot<DbMovie>()
            coEvery { movieDao.updateMovie(capture(dbMovie)) } just runs

            // When
            initializeSut()
            val updatedId = movieRepository.updateMovie(Movie(id = 11, title = "first"))

            // Then
            coVerify { movieDao.updateMovie(any()) }
            assertEquals(
                "first",
                dbMovie.captured.title,
            )
            assertEquals(11, updatedId)
        }

    @Test
    fun `storeMovie just add a new movie`() =
        runTest {
            // Given
            val dbMovie = slot<DbMovie>()
            coEvery { movieDao.insertMovie(capture(dbMovie)) } returns 1L
            coEvery { movieDao.fetchMovieByTmdbId(any()) } returns null

            // When
            initializeSut()
            movieRepository.storeMovie(forCard(title = "first"))

            // Then
            coVerify { movieDao.insertMovie(any()) }
            assertEquals(
                "first",
                dbMovie.captured.title,
            )
        }

    @Test
    fun `storeMovie update existing movie`() =
        runTest {
            // Given
            val dbMovie = slot<DbMovie>()
            coEvery { movieDao.updateMovie(capture(dbMovie)) } just runs
            coEvery { movieDao.fetchMovieByTmdbId(any()) } returns
                DbMovie(
                    id = 1,
                    title = "oldTitle",
                    watchState = WatchState.WATCHED,
                    isArchived = true,
                )

            // When
            initializeSut()
            movieRepository.storeMovie(
                forCard(
                    title = "newTitle",
                    watchState = WatchState.PENDING,
                    isArchived = false,
                ),
            )

            // Then
            coVerify(exactly = 0) { movieDao.insertMovie(any()) }
            coVerify { movieDao.updateMovie(any()) }

            // the title is updated (new data from the backend)
            assertEquals("newTitle", dbMovie.captured.title)
            // the watch state remains untouched (local data in the app)
            assertEquals(WatchState.WATCHED, dbMovie.captured.watchState)
            // the movie is automatically unarchieved
            // (user might have archived it in the past, forgot about it, and wants to re-add)
            assertEquals(false, dbMovie.captured.isArchived)
        }

    @Test
    fun setWatchState() =
        runTest {
            // Given
            coEvery { movieDao.updateWatchState(any(), any()) } just runs

            // When
            initializeSut()
            movieRepository.setWatchState(11, WatchState.WATCHED)

            // Then
            coVerify { movieDao.updateWatchState(11, WatchState.WATCHED) }
        }

    @Test
    fun archiveMovie() =
        runTest {
            // Given
            val movieToArchive =
                Movie(1, "toArchive", watchState = WatchState.WATCHED, isArchived = false)
            val requestedMovie = slot<DbMovie>()
            coEvery { movieDao.deleteMovie(capture(requestedMovie)) } just runs

            // When
            initializeSut()
            movieRepository.archiveMovie(movieToArchive.id)

            // Then
            coVerify { movieDao.archive(movieToArchive.id) }
        }

    @Test
    fun restoreMovie() =
        runTest {
            // Given
            val movieToRestore =
                Movie(1, "isArchived", watchState = WatchState.WATCHED, isArchived = true)
            val requestedMovieId = slot<Long>()
            coEvery { movieDao.restore(capture(requestedMovieId)) } just runs

            // When
            initializeSut()
            movieRepository.restoreMovie(movieToRestore.id)

            // Then
            coVerify { movieDao.restore(movieToRestore.id) }
        }

    @Test
    fun deleteMovie() =
        runTest {
            // Given
            val movieToDelete =
                DbMovie(
                    id = 1,
                    title = "isArchived",
                    watchState = WatchState.WATCHED,
                    isArchived = true,
                )
            val archivedMovie = slot<DbMovie>()
            coEvery { movieDao.deleteMovie(capture(archivedMovie)) } just runs
            coEvery { movieDao.fetchMovieById(1) } returns movieToDelete

            // When
            initializeSut()
            movieRepository.deleteMovie(movieToDelete.id)

            // Then
            coVerify { movieDao.deleteMovie(movieToDelete) }
        }
}
