package com.lairofpixies.whatmovienext.viewmodels

import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ErrorState
import com.lairofpixies.whatmovienext.views.state.ListMode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun `forward archive`() {
        // Given
        val movie = Movie(title = "example movie", isArchived = true)
        every { repo.archivedMovies } returns
            MutableStateFlow(listOf(movie)).asStateFlow()

        // When
        mainViewModel = MainViewModel(repo)

        // Then
        val forwardedMovies = mainViewModel.uiState.value.archiveList
        assertEquals(listOf(movie), forwardedMovies)
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

    @Test
    fun `pass error state and clear`() =
        runTest {
            // Given
            val resultList =
                mutableListOf(
                    mainViewModel.uiState.value.errorState,
                )

            // When
            mainViewModel.showError(ErrorState.SavingWithEmptyTitle)
            resultList.add(mainViewModel.uiState.value.errorState)
            mainViewModel.clearError()
            resultList.add(mainViewModel.uiState.value.errorState)

            // Then
            assertEquals(
                listOf(ErrorState.None, ErrorState.SavingWithEmptyTitle, ErrorState.None),
                resultList.toList(),
            )
        }
}
