package com.lairofpixies.whatmovienext.viewmodel

import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.PartialMovie
import com.lairofpixies.whatmovienext.database.WatchState
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
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
        coVerify { repo.addMovie("adding movie") }
    }

    @Test
    fun `update watch state of movie`() {
        // When
        mainViewModel.updateMovieWatched(1, WatchState.WATCHED)

        // Then
        coVerify { repo.setWatchState(1, WatchState.WATCHED) }
    }

    @Test
    fun `archive movie`() {
        // When
        mainViewModel.archiveMovie(2)

        // Then
        coVerify { repo.archiveMovie(2) }
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
    fun `check unchanged edits`() {
        // Given
        val movie = Movie(id = 1, title = "example movie", watchState = WatchState.PENDING)

        // When
        mainViewModel.beginEditing()
        mainViewModel.saveMovie(movie, {}, {})

        // Then
        val editedMovie = movie.copy()
        assertEquals(false, mainViewModel.hasSaveableChanges(editedMovie))
        assertEquals(false, mainViewModel.hasQuietSaveableChanges(editedMovie))
    }

    @Test
    fun `check saveable edits`() {
        // Given
        val movie = Movie(id = 2, title = "another movie")

        // When
        mainViewModel.beginEditing()
        mainViewModel.saveMovie(movie, {}, {})

        // Then
        val editedMovie = movie.copy(title = "edited movie")
        assertEquals(true, mainViewModel.hasSaveableChanges(editedMovie))
    }

    @Test
    fun `check quiet edits`() {
        // Given
        val movie = Movie(id = 3, title = "quiet movie", watchState = WatchState.PENDING)

        // When
        mainViewModel.beginEditing()
        mainViewModel.saveMovie(movie, {}, {})

        // Then
        val editedMovie = movie.copy(watchState = WatchState.WATCHED)
        assertEquals(false, mainViewModel.hasSaveableChanges(editedMovie))
        assertEquals(true, mainViewModel.hasQuietSaveableChanges(editedMovie))
    }

    @Test
    fun `accept to save movie with title`() {
        // Given
        val movie = Movie(id = 1, title = "successful movie")
        val successCallback = spyk<() -> Unit>()

        // When
        mainViewModel.saveMovie(movie, successCallback) {}

        // Then
        verify { successCallback() }
    }

    @Test
    fun `refuse to save movie without title`() {
        // Given
        val movie = Movie(id = 1, title = "  ")
        val failureCallback = spyk<(ErrorState) -> Unit>()

        // When
        mainViewModel.saveMovie(movie, {}, failureCallback)

        // Then
        verify { failureCallback(ErrorState.SavingWithEmptyTitle) }
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
