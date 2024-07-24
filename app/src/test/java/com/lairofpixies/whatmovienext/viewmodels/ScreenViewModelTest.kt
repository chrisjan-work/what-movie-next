package com.lairofpixies.whatmovienext.viewmodels

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.navigation.Routes
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
class ScreenViewModelTest {
    class TestScreenViewModel : ScreenViewModel() {
        fun verifyMainViewModel(expectedVM: MainViewModel) {
            // Don't expose the protected member
            // only verify it from the protected scope
            assertEquals(expectedVM, mainViewModel)
        }
    }

    lateinit var screenViewModel: TestScreenViewModel

    lateinit var navHostControllerMock: NavHostController
    lateinit var mainViewModelMock: MainViewModel

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
    fun onCancelAction() =
        runTest {
            // When
            screenViewModel.onCancelAction()

            // Then
            coVerify {
                navHostControllerMock.navigate(
                    Routes.HOME.route,
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `close new movie`() =
        runTest {
            // When
            screenViewModel.onCloseWithIdAction(Movie.NEW_ID)

            // Then
            coVerify {
                navHostControllerMock.popBackStack()
            }
        }

    @Test
    fun `close existing movie`() =
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
    fun onNavigateToMovieList() =
        runTest {
            // When
            screenViewModel.onNavigateToMovieList()

            // Then
            coVerify {
                navHostControllerMock.navigate(Routes.AllMoviesView.route)
            }
        }

    @Test
    fun onNavigateToEditCard() =
        runTest {
            // When
            screenViewModel.onNavigateToEditCard(84)

            // Then
            coVerify {
                navHostControllerMock.navigate(Routes.EditMovieView.route(84))
            }
        }
}
