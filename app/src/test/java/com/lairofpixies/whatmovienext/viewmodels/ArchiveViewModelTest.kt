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

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
        val movie = AsyncMovie.Single(forList(title = "example movie", isArchived = true))
        every { repo.archivedMovies } returns
            flowOf(movie)

        // When
        archiveViewModel = ArchiveViewModel(repo)

        // Then
        val archivedMovies = archiveViewModel.archivedMovies.value
        assertEquals(movie, archivedMovies)
    }

    @Test
    fun `select and deselect`() {
        val movie1 = forList(id = 1, title = "first movie")
        val movie2 = forList(id = 2, title = "second movie")

        assertEquals(emptySet<Movie.ForList>(), archiveViewModel.selection.value)

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
                forList(id = 71, title = "archived movie"),
                forList(id = 77, title = "another archived movie"),
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
            val popupInfo = slot<PopupInfo.ConfirmDeletion>()
            every { mainViewModel.showPopup(capture(popupInfo)) } just runs
            archiveViewModel.attachMainViewModel(mainViewModel)

            val moviesToDelete =
                listOf(
                    forList(id = 91, title = "archived movie"),
                    forList(id = 97, title = "another archived movie"),
                )
            moviesToDelete.forEach {
                archiveViewModel.select(it)
            }

            // When
            archiveViewModel.deleteSelectedMovies()
            popupInfo.captured.onConfirm()

            // Then
            coVerify {
                repo.deleteMovie(moviesToDelete[0].appData.movieId)
                repo.deleteMovie(moviesToDelete[1].appData.movieId)
            }
        }
}
