package com.lairofpixies.whatmovienext.viewmodel

import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.PartialMovie
import com.lairofpixies.whatmovienext.database.WatchState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var repo: MovieRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        mainViewModel = MainViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `forward movie list`() {
        // Given
        val movie = Movie(title = "example movie")
        every { repo.movies } returns
            MutableStateFlow(listOf(movie)).asStateFlow()

        // When
        mainViewModel = MainViewModel(repo)

        // Then
        val forwardedMovies = mainViewModel.uiState.value.movieList
        assertEquals(listOf(movie), forwardedMovies)
    }

    @Test
    fun `get single movie`() {
        // Given
        val partialMovie =
            PartialMovie.Completed(
                Movie(id = 10, title = "single movie"),
            )
        every { repo.getMovie(10) } returns
            MutableStateFlow(partialMovie).asStateFlow()

        // When
        val result = mainViewModel.getMovie(10).value

        // Then
        assertEquals(partialMovie, result)
    }

    @Test
    fun `add movie`() {
        // When
        mainViewModel.addMovie("adding movie")

        // Then
        verify { repo.addMovie("adding movie") }
    }

    @Test
    fun `update watch state of movie`() {
        // When
        mainViewModel.updateMovieWatched(1, WatchState.WATCHED)

        // Then
        verify { repo.setWatchState(1, WatchState.WATCHED) }
    }

    @Test
    fun `archive movie`() {
        // When
        mainViewModel.archiveMovie(2)

        // Then
        verify { repo.archiveMovie(2) }
    }

    @Test
    fun `change list mode`() {
        ListMode.entries.forEach { mode ->
            // When
            mainViewModel.setListMode(mode)
            val result = mainViewModel.uiState.value.listMode

            // Then
            assertEquals(mode, result)
        }
    }
}
