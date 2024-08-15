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
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

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
            screenViewModel.onNavigateWithParam(Routes.EditMovieView, 84, false)

            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.EditMovieView.route(84),
                    any<NavOptionsBuilder.() -> Unit>(),
                )
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

    @Test
    fun `roulette feature`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Multiple(
                        listOf(
                            forList(id = 1, title = "Riddick"),
                            forList(id = 2, title = "Pitch Black"),
                            forList(id = 3, title = "The Chronicles of Riddick"),
                        ),
                    ),
                )
            screenViewModel.randomizer = Random(100)
            assertEquals(true, screenViewModel.canSpinRoulette())

            // randomizer with fixed seed will produce fixed sequence
            val expectedSequence: List<Long> = listOf(3, 3, 3, 2, 1, 2)

            expectedSequence.forEach { expectedId ->
                clearMocks(navHostControllerMock)
                // When
                screenViewModel.onNavigateToRandomMovie()

                // Then
                verify {
                    navHostControllerMock.navigate(
                        Routes.SingleMovieView.route(expectedId),
                        any<NavOptionsBuilder.() -> Unit>(),
                    )
                }
            }
        }

    @Test
    fun `roulette feature with taboo`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Multiple(
                        listOf(
                            forList(id = 1, title = "Riddick"),
                            forList(id = 2, title = "Pitch Black"),
                            forList(id = 3, title = "The Chronicles of Riddick"),
                        ),
                    ),
                )
            screenViewModel.randomizer = Random(100)
            assertEquals(true, screenViewModel.canSpinRoulette())

            // randomizer with fixed seed will produce fixed sequence
            val expectedSequence: List<Long> = listOf(3, 2, 3, 1, 3, 2)
            var lastId = 0L

            expectedSequence.forEach { expectedId ->
                clearMocks(navHostControllerMock)
                // When
                screenViewModel.onNavigateToRandomMovie(lastId)
                lastId = expectedId

                // Then
                verify {
                    navHostControllerMock.navigate(
                        Routes.SingleMovieView.route(expectedId),
                        any<NavOptionsBuilder.() -> Unit>(),
                    )
                }
            }
        }
}
