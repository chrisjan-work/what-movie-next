package com.lairofpixies.whatmovienext.viewmodels

import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.PartialMovie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ErrorState
import com.lairofpixies.whatmovienext.views.state.ListMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
import org.junit.Assert.assertNotNull
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
    fun `add movie`() =
        runTest {
            // Given
            val movie = slot<Movie>()
            coEvery { repo.addMovie(capture(movie)) } returns 10

            // When
            val returnedId = mainViewModel.addMovie(Movie(title = "adding movie"))

            // Then
            coVerify { repo.addMovie(any()) }
            assertEquals("adding movie", movie.captured.title)
            assertEquals(10, returnedId)
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
    fun `accept to save new movie with title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "successful movie")
            val successCallback = spyk<(Long) -> Unit>()
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf()

            // When
            mainViewModel.saveMovie(movie, successCallback) {}

            // Then
            coVerify { repo.addMovie(movie) }
            coVerify(exactly = 0) { repo.updateMovie(any()) }
            verify { successCallback(any()) }
        }

    @Test
    fun `accept to update existing movie with title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "successful movie")
            val successCallback = spyk<(Long) -> Unit>()
            coEvery { repo.fetchMovieById(any()) } returns movie
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf()

            // When
            mainViewModel.saveMovie(movie, successCallback) {}

            // Then
            coVerify { repo.updateMovie(movie) }
            coVerify(exactly = 0) { repo.addMovie(any()) }
            verify { successCallback(any()) }
        }

    @Test
    fun `refuse to save movie without title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "  ")
            val failureCallback = spyk<(ErrorState) -> Unit>()

            // When
            mainViewModel.saveMovie(movie, {}, failureCallback)

            // Then
            verify { failureCallback(ErrorState.SavingWithEmptyTitle) }
        }

    @Test
    fun `refuse to save movie with duplicate title`() =
        runTest {
            // Given
            val movieToSave = Movie(id = 1, title = "duplicate movie")
            val duplicatedMovie = Movie(id = 2, title = "duplicate movie")
            var capturedError: ErrorState? = null
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)

            // When
            mainViewModel.saveMovie(movieToSave, {}, onFailure = { capturedError = it })

            // Then
            assertNotNull(capturedError)
            assertEquals(ErrorState.DuplicatedTitle::class.java, capturedError!!::class.java)
        }

    @Test
    fun `user discards movie with duplicate title`() =
        runTest {
            // Given
            val movieToSave = Movie(id = 1, title = "duplicate movie")
            val duplicatedMovie = Movie(id = 2, title = "duplicate movie")
            val successCallback = spyk<(Long) -> Unit>()
            var capturedError: ErrorState? = null
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)

            // When
            mainViewModel.saveMovie(
                movieToSave,
                onSuccess = successCallback,
                onFailure = { capturedError = it },
            )
            (capturedError as? ErrorState.DuplicatedTitle)?.onDiscard?.invoke()

            // Then
            verify { successCallback(movieToSave.id) }
            coVerify(exactly = 0) { repo.addMovie(any()) }
            coVerify(exactly = 0) { repo.updateMovie(any()) }
        }

    @Test
    fun `user overwrites duplicate movie with new entry`() =
        runTest {
            // Given
            val movieToSave =
                Movie(
                    id = 1,
                    title = "duplicate movie",
                    watchState = WatchState.PENDING,
                )
            val duplicatedMovie =
                Movie(
                    id = 2,
                    title = "duplicate movie",
                    watchState = WatchState.WATCHED,
                )
            val successCallback = spyk<(Long) -> Unit>()
            var capturedError: ErrorState? = null
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)

            // When
            mainViewModel.saveMovie(
                movieToSave,
                onSuccess = successCallback,
                onFailure = { capturedError = it },
            )
            (capturedError as? ErrorState.DuplicatedTitle)?.onSave?.invoke()

            // Then
            verify { successCallback(any()) }
            val expectedMovie =
                Movie(
                    id = 2,
                    title = "duplicate movie",
                    watchState = WatchState.PENDING,
                )
            coVerify { repo.updateMovie(expectedMovie) }
            coVerify(exactly = 0) { repo.addMovie(any()) }
            coVerify(exactly = 0) { repo.deleteMovie(movieToSave) }
        }

    @Test
    fun `user overwrites duplicate movie with edited existing entry`() =
        runTest {
            // Given
            val movieToSave =
                Movie(
                    id = 1,
                    title = "duplicate movie",
                    watchState = WatchState.PENDING,
                )
            val duplicatedMovie =
                Movie(
                    id = 2,
                    title = "duplicate movie",
                    watchState = WatchState.WATCHED,
                )
            val successCallback = spyk<(Long) -> Unit>()
            var capturedError: ErrorState? = null
            coEvery { repo.fetchMovieById(any()) } returns movieToSave
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)

            // When
            mainViewModel.saveMovie(
                movieToSave,
                onSuccess = successCallback,
                onFailure = { capturedError = it },
            )
            (capturedError as? ErrorState.DuplicatedTitle)?.onSave?.invoke()

            // Then
            verify { successCallback(any()) }
            val expectedMovie =
                Movie(
                    id = 2,
                    title = "duplicate movie",
                    watchState = WatchState.PENDING,
                )
            coVerify { repo.updateMovie(expectedMovie) }
            coVerify { repo.deleteMovie(movieToSave) }
            coVerify(exactly = 0) { repo.addMovie(any()) }
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

    @Test
    fun `restore movies from archive`() =
        runTest {
            // Given
            val moviesToRestore =
                listOf(
                    Movie(id = 71, title = "archived movie"),
                    Movie(id = 77, title = "another archived movie"),
                )

            // When
            mainViewModel.restoreMovies(moviesToRestore)

            // Then
            coVerify {
                repo.restoreMovie(71)
                repo.restoreMovie(77)
            }
        }

    @Test
    fun `delete movies from archive`() =
        runTest {
            // Given
            val moviesToDelete =
                listOf(
                    Movie(id = 91, title = "archived movie"),
                    Movie(id = 97, title = "another archived movie"),
                )

            // When
            mainViewModel.deleteMovies(moviesToDelete)

            // Then
            coVerify {
                repo.deleteMovie(moviesToDelete[0])
                repo.deleteMovie(moviesToDelete[1])
            }
        }
}
