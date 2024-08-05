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
package com.lairofpixies.whatmovienext.viewmodels

import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.models.data.LoadingMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.clearMocks
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
class EditCardViewModelTest {
    private lateinit var editViewModel: EditCardViewModel
    private lateinit var dbRepoMock: MovieRepository
    private lateinit var apiRepoMock: ApiRepository
    private lateinit var navHostControllerMock: NavHostController
    private lateinit var mainViewModelMock: MainViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dbRepoMock = mockk(relaxed = true)
        apiRepoMock = mockk(relaxed = true)
        navHostControllerMock = mockk(relaxed = true)
        mainViewModelMock = mockk(relaxed = true)

        editViewModel =
            EditCardViewModel(dbRepoMock, apiRepoMock).apply {
                attachNavHostController(navHostControllerMock)
                attachMainViewModel(mainViewModelMock)
            }
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
            coEvery { dbRepoMock.fetchMovieById(any()) } returns returnedMovie

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
            coEvery { dbRepoMock.addMovie(capture(movie)) } returns 10

            // When
            val returnedId = editViewModel.addMovieToDb(Movie(title = "adding movie"))

            // Then
            coVerify { dbRepoMock.addMovie(any()) }
            assertEquals("adding movie", movie.captured.title)
            assertEquals(10, returnedId)
        }

    @Test
    fun `update existing movie data`() =
        runTest {
            // Given
            val movie = slot<Movie>()
            coEvery { dbRepoMock.updateMovie(capture(movie)) } returns 6

            // When
            val returnedId = editViewModel.updateMovieInDb(Movie(id = 6, title = "updating movie"))

            // Then
            coVerify { dbRepoMock.updateMovie(any()) }
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
            coVerify { dbRepoMock.archiveMovie(2) }
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
            coEvery { dbRepoMock.fetchMovieById(any()) } returns null
            coEvery { dbRepoMock.fetchMoviesByTitle(any()) } returns listOf()

            // When
            editViewModel.updateMovieEdits { movie }
            editViewModel.onSaveAction()

            // Then
            coVerify { dbRepoMock.addMovie(movie) }
            coVerify(exactly = 0) { dbRepoMock.updateMovie(any()) }
        }

    @Test
    fun `accept to update existing movie with title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "successful movie")
            coEvery { dbRepoMock.fetchMovieById(any()) } returns movie
            coEvery { dbRepoMock.fetchMoviesByTitle(any()) } returns listOf()

            // When
            editViewModel.updateMovieEdits { movie }
            editViewModel.onSaveAction()

            // Then
            coVerify { dbRepoMock.updateMovie(movie) }
            coVerify(exactly = 0) { dbRepoMock.addMovie(any()) }
        }

    @Test
    fun `refuse to save movie without title`() =
        runTest {
            // Given
            val movie = Movie(id = 1, title = "  ")
            coEvery { mainViewModelMock.showPopup(any<PopupInfo.EmptyTitle>()) } just runs

            // When
            editViewModel.updateMovieEdits { movie }
            editViewModel.onSaveAction()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.EmptyTitle>()) }
        }

    @Test
    fun `refuse to save movie with duplicate title`() =
        runTest {
            // Given
            val movieToSave = Movie(id = 1, title = "duplicate movie")
            val duplicatedMovie = Movie(id = 2, title = "duplicate movie")
            coEvery { dbRepoMock.fetchMovieById(any()) } returns null
            coEvery { dbRepoMock.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
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
            coEvery { dbRepoMock.fetchMovieById(any()) } returns null
            coEvery { dbRepoMock.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
            val capturedError = slot<PopupInfo.DuplicatedTitle>()
            val spy = spyk(editViewModel)
            coEvery { spy.showPopup(capture(capturedError)) } just runs

            // When
            spy.updateMovieEdits { movieToSave }
            spy.onSaveAction()
            capturedError.captured.onDiscard()

            // Then
            coVerify { spy.onCloseWithIdAction(890) }
            coVerify(exactly = 0) { dbRepoMock.addMovie(any()) }
            coVerify(exactly = 0) { dbRepoMock.updateMovie(any()) }
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
            coEvery { dbRepoMock.fetchMovieById(any()) } returns null
            coEvery { dbRepoMock.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
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
            coVerify { dbRepoMock.updateMovie(expectedMovie) }
            coVerify(exactly = 0) { dbRepoMock.addMovie(any()) }
            coVerify(exactly = 0) { dbRepoMock.deleteMovie(movieToSave.id) }
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
            coEvery { dbRepoMock.fetchMovieById(any()) } returns movieToSave
            coEvery { dbRepoMock.fetchMoviesByTitle(any()) } returns listOf(duplicatedMovie)
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
            coVerify { dbRepoMock.updateMovie(expectedMovie) }
            coVerify { dbRepoMock.deleteMovie(movieToSave.id) }
            coVerify(exactly = 0) { dbRepoMock.addMovie(any()) }
        }

    @Test
    fun `attempt searching movie without title`() =
        runTest {
            // Given
            editViewModel.updateMovieEdits { Movie(id = 0, title = "") }

            // When
            editViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.EmptyTitle>()) }
        }

    @Test
    fun `show loading popup while search is ongoing`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                MutableStateFlow(LoadingMovie.Loading).asStateFlow()
            editViewModel.updateMovieEdits { Movie(id = 1, title = "anything") }

            // When
            editViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.Searching>()) }
        }

    @Test
    fun `cancel ongoing search`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                MutableStateFlow(LoadingMovie.Loading).asStateFlow()
            editViewModel.updateMovieEdits { Movie(id = 1, title = "anything") }
            editViewModel.startSearch()
            clearMocks(mainViewModelMock)

            // When
            editViewModel.cancelSearch()

            // Then
            coVerify { mainViewModelMock.closePopupOfType(PopupInfo.Searching::class) }
        }

    @Test
    fun `show error popup when search fails`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                MutableStateFlow(LoadingMovie.Failed(Exception())).asStateFlow()
            editViewModel.updateMovieEdits { Movie(id = 1, title = "anything") }

            // When
            editViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.ConnectionFailed>()) }
        }

    @Test
    fun `empty search results`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                MutableStateFlow(LoadingMovie.Empty).asStateFlow()
            editViewModel.updateMovieEdits { Movie(id = 1, title = "anything") }

            // When
            editViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.SearchEmpty>()) }
        }

    @Test
    fun `update edit movie when search delivers a single result`() =
        runTest {
            // Given
            val movie = Movie(id = 1007, tmdbId = 1007, title = "From Russia with Love")
            val movieFlow = MutableStateFlow(LoadingMovie.Single(movie)).asStateFlow()
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns movieFlow
            coEvery { apiRepoMock.getMovieDetails(any()) } returns movieFlow
            editViewModel.updateMovieEdits { Movie(id = 1, title = "anything") }

            // When
            editViewModel.startSearch()

            // Then
            assertEquals(movie, editViewModel.currentMovie.value)
        }

    @Test
    fun `load search results when search delivers more than one match`() =
        runTest {
            // Given
            val loadingMovies =
                LoadingMovie.Multiple(
                    listOf(
                        Movie(id = 2007, title = "Live and let die"),
                        Movie(id = 3007, title = "Moonraker"),
                        Movie(id = 4007, title = "Octopussy"),
                    ),
                )
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                MutableStateFlow(loadingMovies).asStateFlow()
            editViewModel.updateMovieEdits { Movie(id = 1, title = "anything") }

            // When
            editViewModel.startSearch()

            // Then
            assertEquals(loadingMovies, editViewModel.searchResults.value)
        }

    @Test
    fun `back button close search results`() =
        runTest {
            // Given
            val loadingMovies =
                LoadingMovie.Multiple(
                    listOf(
                        Movie(id = 2007, title = "Live and let die"),
                        Movie(id = 3007, title = "Moonraker"),
                        Movie(id = 4007, title = "Octopussy"),
                    ),
                )
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                MutableStateFlow(loadingMovies).asStateFlow()
            editViewModel.updateMovieEdits { Movie(id = 1, title = "anything") }
            editViewModel.startSearch()

            // When
            editViewModel.handleBackButton()

            // Then
            assertEquals(LoadingMovie.Empty, editViewModel.searchResults.value)
        }

    @Test
    fun `fetch movie details`() =
        runTest {
            // Given
            val movie = Movie(id = 1007, tmdbId = 1007, title = "From Russia with Love")
            val movieFlow = MutableStateFlow(LoadingMovie.Single(movie)).asStateFlow()
            coEvery { apiRepoMock.getMovieDetails(any()) } returns movieFlow

            // When
            editViewModel.fetchFromRemote(movie)

            // Then
            assertEquals(movie, editViewModel.currentMovie.value)
        }

    @Test
    fun `error on fetch movie details`() =
        runTest {
            // Given

            val failedFlow = MutableStateFlow(LoadingMovie.Failed(Exception())).asStateFlow()
            coEvery { apiRepoMock.getMovieDetails(any()) } returns failedFlow
            val spy = spyk(editViewModel)
            val currentMovie = Movie(id = 1, title = "nothing")
            spy.updateMovieEdits { currentMovie }

            // When
            val fetchableMovie = Movie(id = 1007, tmdbId = 1007, title = "From Russia with Love")
            spy.fetchFromRemote(fetchableMovie)

            // Then
            verify { spy.showPopup(any<PopupInfo.ConnectionFailed>()) }
            assertEquals(currentMovie, editViewModel.currentMovie.value)
        }
}
