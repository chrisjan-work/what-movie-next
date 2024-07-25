package com.lairofpixies.whatmovienext.viewmodels

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ErrorState
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.UiState
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
class MovieListViewModelTest {
    private lateinit var listViewModel: MovieListViewModel
    private lateinit var mainViewModelMock: MainViewModel
    private lateinit var repo: MovieRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)

        mainViewModelMock = mockk(relaxed = true)
        every { mainViewModelMock.uiState } returns
            MutableStateFlow(
                UiState(
                    listMode = ListMode.ALL,
                    errorState = ErrorState.None,
                ),
            )
    }

    private fun rerunConstructor() {
        listViewModel = MovieListViewModel(repo)
        listViewModel.attachMainViewModel(mainViewModelMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `forward movie list with all movies filter`() =
        runTest {
            // Given
            val seenMovie = Movie(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                Movie(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.movies } returns
                MutableStateFlow(listOf(unseenMovie, seenMovie)).asStateFlow()
            every { mainViewModelMock.uiState } returns
                MutableStateFlow(
                    UiState(
                        listMode = ListMode.ALL,
                        errorState = ErrorState.None,
                    ),
                )

            // When
            rerunConstructor()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovieInfo.Multiple(listOf(unseenMovie, seenMovie)), forwardedMovies)
        }

    @Test
    fun `forward movie list with only unseen movies`() =
        runTest {
            // Given
            val seenMovie = Movie(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                Movie(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.movies } returns
                MutableStateFlow(listOf(unseenMovie, seenMovie)).asStateFlow()
            every { mainViewModelMock.uiState } returns
                MutableStateFlow(
                    UiState(
                        listMode = ListMode.PENDING,
                        errorState = ErrorState.None,
                    ),
                )

            // When
            rerunConstructor()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovieInfo.Single(unseenMovie), forwardedMovies)
        }

    @Test
    fun `forward movie list with only seen movies`() =
        runTest {
            // Given
            val seenMovie = Movie(id = 23, title = "The Number 23", watchState = WatchState.WATCHED)
            val unseenMovie =
                Movie(id = 9, title = "Plan 9 from Outer Space", watchState = WatchState.PENDING)
            every { repo.movies } returns
                MutableStateFlow(listOf(unseenMovie, seenMovie)).asStateFlow()
            every { mainViewModelMock.uiState } returns
                MutableStateFlow(
                    UiState(
                        listMode = ListMode.WATCHED,
                        errorState = ErrorState.None,
                    ),
                )

            // When
            rerunConstructor()

            // Then
            val forwardedMovies = listViewModel.listedMovies.value
            assertEquals(AsyncMovieInfo.Single(seenMovie), forwardedMovies)
        }

    @Test
    fun `forward list mode`() =
        runTest {
            // Given
            every { mainViewModelMock.uiState } returns
                MutableStateFlow(
                    UiState(
                        listMode = ListMode.WATCHED,
                        errorState = ErrorState.None,
                    ),
                )

            // When
            rerunConstructor()

            // Then
            assertEquals(ListMode.WATCHED, listViewModel.listMode.value)
        }

    @Test
    fun `detect if the archive is empty`() {
        // Given
        every { repo.archivedMovies } returns
            MutableStateFlow(emptyList<Movie>()).asStateFlow()

        // When
        rerunConstructor()

        // Then
        val hasArchives = listViewModel.hasArchivedMovies.value
        assertEquals(false, hasArchives)
    }

    @Test
    fun `detect if there are archived movies`() {
        // Given
        val movie = Movie(title = "example movie", isArchived = true)
        every { repo.archivedMovies } returns
            MutableStateFlow(listOf(movie)).asStateFlow()

        // When
        rerunConstructor()

        // Then
        val hasArchives = listViewModel.hasArchivedMovies.value
        assertEquals(true, hasArchives)
    }
}
