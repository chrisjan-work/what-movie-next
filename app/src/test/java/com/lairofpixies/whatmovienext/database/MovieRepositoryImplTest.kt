package com.lairofpixies.whatmovienext.database

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
            assertEquals(movies, result)
        }

    @Test
    fun getMovie() {
        // Given
        val movie = Movie(1, "first", WatchState.WATCHED)
        coEvery { movieDao.getMovie(1) } returns flowOf(movie)

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        val result = sut.getMovie(1).value

        // Then
        assertEquals(PartialMovie.Completed(movie), result)
    }

    @Test
    fun `getMovie loading`() {
        // Given
        coEvery { movieDao.getMovie(1) } returns emptyFlow()

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        val result = sut.getMovie(1).value

        // Then
        assertEquals(PartialMovie.Loading, result)
    }

    @Test
    fun `getMovie not found`() {
        // Given
        coEvery { movieDao.getMovie(1) } returns flowOf(null)

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        val result = sut.getMovie(1).value

        // Then
        assertEquals(PartialMovie.NotFound, result)
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
            coEvery { movieDao.updateMovieDetails(capture(movie)) } just runs

            // When
            sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
            val updatedId = sut.updateMovie(Movie(id = 11, title = "first"))

            // Then
            coVerify { movieDao.updateMovieDetails(any()) }
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
}
