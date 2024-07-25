package com.lairofpixies.whatmovienext.viewmodels

import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class EditCardViewModelTest {
    private lateinit var editViewModel: EditCardViewModel
    private lateinit var repo: MovieRepository
    private lateinit var navHostControllerMock: NavHostController
    private lateinit var mainViewModelMock: MainViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        editViewModel = EditCardViewModel(repo)
        navHostControllerMock = mockk(relaxed = true)
        editViewModel.attachNavHostController(navHostControllerMock)
        mainViewModelMock = mockk(relaxed = true)
        editViewModel.attachMainViewModel(mainViewModelMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `access currently edited movie`() {
        // default movie
        assertEquals(Movie(id = Movie.NEW_ID, title = ""), editViewModel.currentMovie.value)

        // declare edits
        editViewModel.updateMovieEdits { copy(id = 99, title = "edited title") }

        // verify edits
        assertEquals(Movie(id = 99, title = "edited title"), editViewModel.currentMovie.value)
    }

    @Test
    fun loadMovieForEdit() =
        runTest {
            // Given
            val returnedMovie = Movie(id = 778, title = "Film No. 778")
            coEvery { repo.fetchMovieById(any()) } returns returnedMovie

            // When
            editViewModel.loadMovieForEdit(778)

            // Then
            assertEquals(returnedMovie, editViewModel.currentMovie.value)
        }

    @Test
    fun `add movie to database`() =
        runTest {
            // Given
            val movie = slot<Movie>()
            coEvery { repo.addMovie(capture(movie)) } returns 10

            // When
            val returnedId = editViewModel.addMovieToDb(Movie(title = "adding movie"))

            // Then
            coVerify { repo.addMovie(any()) }
            assertEquals("adding movie", movie.captured.title)
            assertEquals(10, returnedId)
        }

    @Test
    fun `update existing movie data`() =
        runTest {
            // Given
            val movie = slot<Movie>()
            coEvery { repo.updateMovie(capture(movie)) } returns 6

            // When
            val returnedId = editViewModel.updateMovieInDb(Movie(id = 6, title = "updating movie"))

            // Then
            coVerify { repo.updateMovie(any()) }
            assertEquals("updating movie", movie.captured.title)
            assertEquals(6, returnedId)
        }

    @Test
    fun `archive currently edited movie`() =
        runTest {
            // Given
            editViewModel.updateMovieEdits { copy(id = 2) }

            // When
            editViewModel.archiveCurrentMovie()

            // Then
            coVerify { repo.archiveMovie(2) }
        }

    @Test
    fun `back button pass through when there are no saveable edits`() {
        // Given
        val movie = Movie(id = 89, title = "The Bourne Identity", watchState = WatchState.PENDING)
        val spy = spyk(editViewModel)
        every { spy.onCloseWithIdAction(any()) } just runs
        spy.updateMovieEdits(resetSaved = true) { movie }

        // When
        spy.handleBackButton()

        // Then
        verify { spy.onCloseWithIdAction(89) }
        verify(exactly = 0) { spy.onSaveAction() }
        verify(exactly = 0) { spy.showPopup(any<PopupInfo.UnsavedChanges>()) }
    }

    @Test
    fun `back button alert user when there are saveable edits`() {
        // Given
        val movie = Movie(id = 117, title = "OSS 117 Cairo, Nest of Spies")
        val spy = spyk(editViewModel)
        every { spy.showPopup(any<PopupInfo.UnsavedChanges>()) } just runs
        spy.updateMovieEdits(resetSaved = false) { movie }

        // When
        spy.handleBackButton()

        // Then
        verify(exactly = 0) { spy.onCloseWithIdAction(117) }
        verify(exactly = 0) { spy.onSaveAction() }
        verify(exactly = 1) { spy.showPopup(any<PopupInfo.UnsavedChanges>()) }
    }

    @Test
    fun `back button alerts user and user saves the changes`() {
        // Given
        val movie = Movie(id = 117, title = "OSS 117 Cairo, Nest of Spies")
        val spy = spyk(editViewModel)
        val capturedError = slot<PopupInfo.UnsavedChanges>()
        every { spy.showPopup(capture(capturedError)) } just runs
        spy.updateMovieEdits(resetSaved = false) { movie }

        // When
        spy.handleBackButton()
        capturedError.captured.onSave()

        // Then
        verify(exactly = 0) { spy.onCloseWithIdAction(117) }
        verify(exactly = 1) { spy.onSaveAction() }
    }

    @Test
    fun `back button alerts user and user ignores the changes`() {
        // Given
        val movie = Movie(id = 117, title = "OSS 117 Cairo, Nest of Spies")
        val spy = spyk(editViewModel)
        val capturedError = slot<PopupInfo.UnsavedChanges>()
        every { spy.showPopup(capture(capturedError)) } just runs
        spy.updateMovieEdits(resetSaved = false) { movie }

        // When
        spy.handleBackButton()
        capturedError.captured.onDiscard()

        // Then
        verify(exactly = 1) { spy.onCloseWithIdAction(117) }
        verify(exactly = 0) { spy.onSaveAction() }
    }

    @Test
    fun `back button quietly save when there are quiet changes`() {
        // Given
        val movie = Movie(id = 201, title = "The Artist", watchState = WatchState.PENDING)
        val spy = spyk(editViewModel)
        every { spy.onSaveAction() } just runs
        spy.updateMovieEdits(resetSaved = true) { movie }

        // When
        spy.updateMovieEdits(resetSaved = false) { copy(watchState = WatchState.WATCHED) }
        spy.handleBackButton()

        // Then
        verify(exactly = 0) { spy.onCloseWithIdAction(201) }
        verify(exactly = 1) { spy.onSaveAction() }
        verify(exactly = 0) { spy.showPopup(any<PopupInfo.UnsavedChanges>()) }
    }

    @Test
    fun `accept to save new movie with title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "successful movie")
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf()

            // When
            editViewModel.updateMovieEdits { movie }
            editViewModel.onSaveAction()

            // Then
            coVerify { repo.addMovie(movie) }
            coVerify(exactly = 0) { repo.updateMovie(any()) }
        }

    @Test
    fun `accept to update existing movie with title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "successful movie")
            coEvery { repo.fetchMovieById(any()) } returns movie
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf()

            // When
            editViewModel.updateMovieEdits { movie }
            editViewModel.onSaveAction()

            // Then
            coVerify { repo.updateMovie(movie) }
            coVerify(exactly = 0) { repo.addMovie(any()) }
        }

    @Test
    fun `refuse to save movie without title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "  ")
            coEvery { mainViewModelMock.showPopup(any<PopupInfo.SavingWithEmptyTitle>()) } just runs

            // When
            editViewModel.updateMovieEdits { movie }
            editViewModel.onSaveAction()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.SavingWithEmptyTitle>()) }
        }

    @Test
    fun `refuse to save movie with duplicate title`() =
        runTest {
            // Given
            val movieToSave = Movie(id = 1, title = "duplicate movie")
            val duplicatedMovie = Movie(id = 2, title = "duplicate movie")
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
            coEvery { mainViewModelMock.showPopup(any<PopupInfo.DuplicatedTitle>()) } just runs

            // When
            editViewModel.updateMovieEdits { movieToSave }
            editViewModel.onSaveAction()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.DuplicatedTitle>()) }
        }

    @Test
    fun `user discards movie with duplicate title`() =
        runTest {
            // Given
            val movieToSave = Movie(id = 890, title = "duplicate movie")
            val duplicatedMovie = Movie(id = 293, title = "duplicate movie")
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
            val capturedError = slot<PopupInfo.DuplicatedTitle>()
            val spy = spyk(editViewModel)
            coEvery { spy.showPopup(capture(capturedError)) } just runs

            // When
            spy.updateMovieEdits { movieToSave }
            spy.onSaveAction()
            capturedError.captured.onDiscard()

            // Then
            coVerify { spy.onCloseWithIdAction(890) }
            coVerify(exactly = 0) { repo.addMovie(any()) }
            coVerify(exactly = 0) { repo.updateMovie(any()) }
        }

    @Test
    fun `user overwrites duplicate movie with new entry`() =
        runTest {
            // Given
            val movieToSave =
                Movie(
                    id = 543,
                    title = "duplicate movie",
                    watchState = WatchState.PENDING,
                )
            val duplicatedMovie =
                Movie(
                    id = 345,
                    title = "duplicate movie",
                    watchState = WatchState.WATCHED,
                )
            coEvery { repo.fetchMovieById(any()) } returns null
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
            val capturedError = slot<PopupInfo.DuplicatedTitle>()
            val spy = spyk(editViewModel)
            coEvery { spy.showPopup(capture(capturedError)) } just runs

            // When
            spy.updateMovieEdits { movieToSave }
            spy.onSaveAction()
            capturedError.captured.onSave()

            // Then
            val expectedMovie =
                Movie(
                    id = 345,
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
                    id = 579,
                    title = "duplicate movie",
                    watchState = WatchState.PENDING,
                )
            val duplicatedMovie =
                Movie(
                    id = 975,
                    title = "duplicate movie",
                    watchState = WatchState.WATCHED,
                )
            coEvery { repo.fetchMovieById(any()) } returns movieToSave
            coEvery { repo.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
            val capturedError = slot<PopupInfo.DuplicatedTitle>()
            val spy = spyk(editViewModel)
            coEvery { spy.showPopup(capture(capturedError)) } just runs

            // When
            spy.updateMovieEdits { movieToSave }
            spy.onSaveAction()
            capturedError.captured.onSave()

            // Then
            val expectedMovie =
                Movie(
                    id = 975,
                    title = "duplicate movie",
                    watchState = WatchState.PENDING,
                )
            coVerify { repo.updateMovie(expectedMovie) }
            coVerify { repo.deleteMovie(movieToSave) }
            coVerify(exactly = 0) { repo.addMovie(any()) }
        }
}
