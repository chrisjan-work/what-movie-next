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
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.coVerify
import io.mockk.mockk
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
class ScreenViewModelTest {
    class TestScreenViewModel : ScreenViewModel() {
        fun verifyMainViewModel(expectedVM: MainViewModel) {
            // Don't expose the protected member
            // only verify it from the protected scope
            assertEquals(expectedVM, mainViewModel)
        }
    }

    private lateinit var screenViewModel: TestScreenViewModel

    private lateinit var navHostControllerMock: NavHostController
    private lateinit var mainViewModelMock: MainViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        navHostControllerMock = mockk(relaxed = true)
        mainViewModelMock = mockk(relaxed = true)
        screenViewModel = TestScreenViewModel()
        screenViewModel.attachNavHostController(navHostControllerMock)
        screenViewModel.attachMainViewModel(mainViewModelMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `access main view model from subclasses if needed`() {
        screenViewModel.verifyMainViewModel(mainViewModelMock)
    }

    @Test
    fun `onCancelAction navigates to the home route`() =
        runTest {
            // When
            screenViewModel.onLeaveAction()

            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.HOME.route,
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `close edit card with new movie just navigates back one step in stack`() =
        runTest {
            // When
            screenViewModel.onCloseWithIdAction(Movie.NEW_ID)

            // Then
            coVerify {
                navHostControllerMock.popBackStack()
            }
        }

    @Test
    fun `close edit card of existing movie navigates to single movie view`() =
        runTest {
            // When
            screenViewModel.onCloseWithIdAction(111)

            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.SingleMovieView.route(111),
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `navigate to movie list route`() =
        runTest {
            // When
            screenViewModel.onNavigateTo(Routes.AllMoviesView)

            // Then
            coVerify {
                navHostControllerMock.navigate(Routes.AllMoviesView.route)
            }
        }

    @Test
    fun `navigate to archive route`() =
        runTest {
            // When
            screenViewModel.onNavigateTo(Routes.ArchiveView)

            // Then
            coVerify {
                navHostControllerMock.navigate(Routes.ArchiveView.route)
            }
        }

    @Test
    fun `navigate to edit card route with given id`() =
        runTest {
            // When
            screenViewModel.onNavigateWithParam(Routes.EditMovieView, 84)

            // Then
            coVerify {
                navHostControllerMock.navigate(Routes.EditMovieView.route(84))
            }
        }

    @Test
    fun `forward showpopup calls`() {
        // When
        screenViewModel.showPopup(PopupInfo.SearchEmpty)

        // Then
        verify { mainViewModelMock.showPopup(PopupInfo.SearchEmpty) }
    }

    @Test
    fun `forward general closepopup calls`() {
        // When
        screenViewModel.closePopup()

        // Then
        verify { mainViewModelMock.closePopup() }
    }

    @Test
    fun `forward closepopup calls for specific popup types`() {
        // When
        screenViewModel.closePopupOfType(PopupInfo.SearchEmpty::class)

        // Then
        verify { mainViewModelMock.closePopupOfType(PopupInfo.SearchEmpty::class) }
    }
}
