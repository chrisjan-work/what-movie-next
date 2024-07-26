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
        mainViewModel.showPopup(PopupInfo.SearchFailed)

        // When
        mainViewModel.closePopupOfType(PopupInfo.DuplicatedTitle::class)

        // Then
        assertEquals(PopupInfo.SearchFailed, mainViewModel.popupInfo.value)
    }

    @Test
    fun `close popup of a given type`() {
        // Given
        mainViewModel.showPopup(PopupInfo.SearchFailed)

        // When
        mainViewModel.closePopupOfType(PopupInfo.SearchFailed::class)

        // Then
        assertEquals(PopupInfo.None, mainViewModel.popupInfo.value)
    }
}
