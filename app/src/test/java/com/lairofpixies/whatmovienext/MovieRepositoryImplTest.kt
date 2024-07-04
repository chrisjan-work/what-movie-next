package com.lairofpixies.whatmovienext

import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieDao
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.MovieRepositoryImpl
import com.lairofpixies.whatmovienext.database.WatchState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    fun getMovies() {
        // Given
        val movies =
            listOf(
                Movie(1, "first", WatchState.WATCHED),
                Movie(2, "second", WatchState.WATCHED),
            )
        coEvery { movieDao.getAllMovies() } returns flowOf(movies)

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        val result = sut.movies.value

        // Then
        assertEquals(movies, result)
    }

    @Test
    fun addMovie() {
        // Given
        val movieList = slot<List<Movie>>()
        coEvery { movieDao.insertMovies(capture(movieList)) } just runs

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        sut.addMovie("first")

        // Then
        coVerify { movieDao.insertMovies(any()) }
        assertEquals(
            "first",
            movieList.captured.firstOrNull()?.title,
        )
    }

    @Test
    fun setWatchState() {
        // Given
        coEvery { movieDao.updateWatchState(any(), any()) } just runs

        // When
        sut = MovieRepositoryImpl(movieDao, UnconfinedTestDispatcher())
        sut.setWatchState(11, WatchState.WATCHED)

        // Then
        coVerify { movieDao.updateWatchState(11, WatchState.WATCHED) }
    }

    @Test
    fun archiveMovie() {
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
