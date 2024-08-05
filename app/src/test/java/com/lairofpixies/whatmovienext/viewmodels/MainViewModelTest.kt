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

import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.PopupInfo
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
    private lateinit var mainViewModel: MainViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mainViewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `change list mode`() {
        ListMode.entries.forEach { mode ->
            // When
            mainViewModel.setListMode(mode)
            val result = mainViewModel.movieListDisplayState.value.listMode

            // Then
            assertEquals(mode, result)
        }
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
}
