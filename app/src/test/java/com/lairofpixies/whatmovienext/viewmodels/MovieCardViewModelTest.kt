package com.lairofpixies.whatmovienext.viewmodels

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
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
class MovieCardViewModelTest {
    private lateinit var cardViewModel: MovieCardViewModel
    private lateinit var repo: MovieRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        cardViewModel = MovieCardViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `get single movie`() {
        // Given
        val partialMovie =
            AsyncMovieInfo.Single(
                Movie(id = 10, title = "single movie"),
            )
        every { repo.getMovie(10) } returns
            MutableStateFlow(partialMovie).asStateFlow()

        // When
        val result = cardViewModel.getMovie(10).value

        // Then
        assertEquals(partialMovie, result)
    }

    @Test
    fun `update watch state of movie`() {
        // When
        cardViewModel.updateMovieWatched(10, WatchState.WATCHED)
        cardViewModel.updateMovieWatched(31, WatchState.PENDING)

        // Then
        coVerifyOrder {
            repo.setWatchState(10, WatchState.WATCHED)
            repo.setWatchState(31, WatchState.PENDING)
        }
    }
}
