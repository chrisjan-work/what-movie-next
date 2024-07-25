package com.lairofpixies.whatmovienext.viewmodels

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.toList
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.ErrorState
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
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
class ArchiveViewModelTest {
    private lateinit var archiveViewModel: ArchiveViewModel
    private lateinit var repo: MovieRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        archiveViewModel = ArchiveViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getArchivedMovies() {
        // Given
        val movie = Movie(title = "example movie", isArchived = true)
        every { repo.archivedMovies } returns
            MutableStateFlow(
                AsyncMovieInfo.Single(movie),
            ).asStateFlow()

        // When
        archiveViewModel = ArchiveViewModel(repo)

        // Then
        val archivedMovies = archiveViewModel.archivedMovies.value
        assertEquals(listOf(movie), archivedMovies.toList())
    }

    @Test
    fun `select and deselect`() {
        val movie1 = Movie(id = 1, title = "first movie")
        val movie2 = Movie(id = 2, title = "second movie")

        assertEquals(emptySet<Movie>(), archiveViewModel.selection.value)

        // select
        archiveViewModel.select(movie1)
        assertEquals(setOf(movie1), archiveViewModel.selection.value)
        archiveViewModel.select(movie2)
        assertEquals(setOf(movie1, movie2), archiveViewModel.selection.value)

        // deselect
        archiveViewModel.deselect(movie1)
        assertEquals(setOf(movie2), archiveViewModel.selection.value)
    }

    @Test
    fun `restore movies from archive`() =
        runTest {
            // Given
            listOf(
                Movie(id = 71, title = "archived movie"),
                Movie(id = 77, title = "another archived movie"),
            ).forEach { archiveViewModel.select(it) }

            // When
            archiveViewModel.restoreSelectedMovies()

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
            val mainViewModel: MainViewModel = mockk(relaxed = true)
            val errorState = slot<ErrorState.ConfirmDeletion>()
            every { mainViewModel.showError(capture(errorState)) } just runs
            archiveViewModel.attachMainViewModel(mainViewModel)

            val moviesToDelete =
                listOf(
                    Movie(id = 91, title = "archived movie"),
                    Movie(id = 97, title = "another archived movie"),
                )
            moviesToDelete.forEach {
                archiveViewModel.select(it)
            }

            // When
            archiveViewModel.deleteSelectedMovies()
            errorState.captured.onConfirm()

            // Then
            coVerify {
                repo.deleteMovie(moviesToDelete[0])
                repo.deleteMovie(moviesToDelete[1])
            }
        }
}
