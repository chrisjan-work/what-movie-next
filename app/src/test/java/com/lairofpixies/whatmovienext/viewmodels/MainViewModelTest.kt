package com.lairofpixies.whatmovienext.viewmodels

import com.lairofpixies.whatmovienext.views.state.ErrorState
import com.lairofpixies.whatmovienext.views.state.ListMode
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
            val result = mainViewModel.uiState.value.listMode

            // Then
            assertEquals(mode, result)
        }
    }

    @Test
    fun `pass error state and clear`() =
        runTest {
            // Given
            val resultList =
                mutableListOf(
                    mainViewModel.uiState.value.errorState,
                )

            // When
            mainViewModel.showError(ErrorState.SavingWithEmptyTitle)
            resultList.add(mainViewModel.uiState.value.errorState)
            mainViewModel.clearError()
            resultList.add(mainViewModel.uiState.value.errorState)

            // Then
            assertEquals(
                listOf(ErrorState.None, ErrorState.SavingWithEmptyTitle, ErrorState.None),
                resultList.toList(),
            )
        }
}
