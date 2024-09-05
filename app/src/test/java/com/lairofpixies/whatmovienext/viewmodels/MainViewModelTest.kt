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
import androidx.navigation.NavOptionsBuilder
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
class MainViewModelTest {
    private lateinit var navHostControllerMock: NavHostController
    private lateinit var movieRepositoryMock: MovieRepository

    private lateinit var mainViewModel: MainViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        navHostControllerMock = mockk(relaxed = true)
        movieRepositoryMock = mockk(relaxed = true)
        mainViewModel = MainViewModel(movieRepositoryMock)
        mainViewModel.attachNavHostController(navHostControllerMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pass popup info, and then clear`() =
        runTest {
            // Given
            val resultList =
                mutableListOf(
                    mainViewModel.popupInfo.value,
                )

            // When
            mainViewModel.showPopup(PopupInfo.EmptyTitle)
            resultList.add(mainViewModel.popupInfo.value)
            mainViewModel.closePopup()
            resultList.add(mainViewModel.popupInfo.value)

            // Then
            assertEquals(
                listOf(PopupInfo.None, PopupInfo.EmptyTitle, PopupInfo.None),
                resultList.toList(),
            )
        }

    @Test
    fun `attempt to close popup that was not open because there is a different popup`() {
        // Given
        mainViewModel.showPopup(PopupInfo.ConnectionFailed)

        // When
        mainViewModel.closePopupOfType(PopupInfo.SearchEmpty::class)

        // Then
        assertEquals(PopupInfo.ConnectionFailed, mainViewModel.popupInfo.value)
    }

    @Test
    fun `close popup of a given type`() {
        // Given
        mainViewModel.showPopup(PopupInfo.ConnectionFailed)

        // When
        mainViewModel.closePopupOfType(PopupInfo.ConnectionFailed::class)

        // Then
        assertEquals(PopupInfo.None, mainViewModel.popupInfo.value)
    }

    @Test
    fun `onCancelAction navigates to the home route`() =
        runTest {
            // When
            mainViewModel.onLeaveAction()

            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.HOME.route,
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `navigate to movie list route`() =
        runTest {
            // When
            mainViewModel.onNavigateTo(Routes.AllMoviesView)

            // Then
            coVerify {
                navHostControllerMock.navigate(Routes.AllMoviesView.route)
            }
        }

    @Test
    fun `navigate to archive route`() =
        runTest {
            // When
            mainViewModel.onNavigateTo(Routes.ArchiveView)

            // Then
            coVerify {
                navHostControllerMock.navigate(Routes.ArchiveView.route)
            }
        }

    @Test
    fun `navigate to search result route with given id`() =
        runTest {
            // When
            mainViewModel.onNavigateWithParam(Routes.SharedMovieView, 84, false)

            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.SharedMovieView.route(84),
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `navigate to existing movie given tmdbid`() =
        runTest {
            // Given
            coEvery { movieRepositoryMock.fetchMovieIdFromTmdbId(111) } returns 77
            // When
            mainViewModel.loadAndNavigateTo("/111")
            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.SingleMovieView.route(77),
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `navigate to search movie given tmdbid`() =
        runTest {
            // Given
            coEvery { movieRepositoryMock.fetchMovieIdFromTmdbId(111) } returns null
            // When
            mainViewModel.loadAndNavigateTo("/111")
            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.SharedMovieView.route(111),
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `show error when trying to navigate to invalid route`() =
        runTest {
            // When
            mainViewModel.loadAndNavigateTo("/asdf")
            // Then
            assertEquals(PopupInfo.MovieNotFound, mainViewModel.popupInfo.value)
        }
}
