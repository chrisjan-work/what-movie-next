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
import com.lairofpixies.whatmovienext.models.data.TestMovie.forCard
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
class SharedMovieViewModelTest {
    private lateinit var dbRepoMock: MovieRepository
    private lateinit var apiRepoMock: ApiRepository
    private lateinit var mainViewModelMock: MainViewModel

    private lateinit var sharedMovieViewModel: SharedMovieViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dbRepoMock = mockk(relaxed = true)
        apiRepoMock = mockk(relaxed = true)
        mainViewModelMock = mockk(relaxed = true)

        sharedMovieViewModel =
            SharedMovieViewModel(dbRepoMock, apiRepoMock).apply {
                attachMainViewModel(mainViewModelMock)
            }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `find movie`() =
        runTest {
            // Given
            val expectedMovie =
                AsyncMovie.Single(
                    forCard(title = "Found Movie"),
                )
            coEvery { apiRepoMock.getMovieDetails(1234L) } returns
                flowOf(expectedMovie)

            // When
            sharedMovieViewModel.fetchFromRemote(1234L)

            // Then
            assertEquals(expectedMovie, sharedMovieViewModel.foundMovie.value)
            coVerify(exactly = 0) {
                mainViewModelMock.onLeaveAction()
            }
        }

    @Test
    fun `miss movie`() {
        // Given
        val throwable = Exception("Simulated Error")
        coEvery { apiRepoMock.getMovieDetails(1234L) } returns
            flowOf(AsyncMovie.Failed(throwable))

        // When
        sharedMovieViewModel.fetchFromRemote(1234L)

        // Then
        coVerify {
            mainViewModelMock.showPopup(PopupInfo.MovieNotFound)
            mainViewModelMock.onLeaveAction()
        }
    }

    @Test
    fun `find movie and save`() {
        // Given
        val expectedMovie = forCard(title = "Found Movie", tmdbId = 1234L)

        coEvery { apiRepoMock.getMovieDetails(1234L) } returns
            flowOf(AsyncMovie.Single(expectedMovie))
        sharedMovieViewModel.fetchFromRemote(1234L)

        // When
        sharedMovieViewModel.onSaveAction()

        // Then
        coVerify {
            dbRepoMock.storeMovie(expectedMovie)
            mainViewModelMock.onLeaveAction()
        }
    }
}
