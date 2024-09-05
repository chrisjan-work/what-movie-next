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

import com.lairofpixies.whatmovienext.views.state.PopupInfo
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
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
    private lateinit var mainViewModelMock: MainViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mainViewModelMock = mockk(relaxed = true)
        screenViewModel = TestScreenViewModel()
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
