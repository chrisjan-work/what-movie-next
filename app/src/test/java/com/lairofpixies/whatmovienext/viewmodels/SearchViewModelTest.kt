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
import com.lairofpixies.whatmovienext.models.data.LoadingAMovie
import com.lairofpixies.whatmovienext.models.data.SearchQuery
import com.lairofpixies.whatmovienext.models.data.TestAMovie.forCard
import com.lairofpixies.whatmovienext.models.data.TestAMovie.forSearch
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import com.lairofpixies.whatmovienext.views.state.SearchState
import io.mockk.clearMocks
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
import org.junit.Ignore
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var dbRepoMock: MovieRepository
    private lateinit var apiRepoMock: ApiRepository
    private lateinit var navHostControllerMock: NavHostController
    private lateinit var mainViewModelMock: MainViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dbRepoMock = mockk(relaxed = true)
        apiRepoMock = mockk(relaxed = true)
        navHostControllerMock = mockk(relaxed = true)
        mainViewModelMock = mockk(relaxed = true)

        searchViewModel =
            SearchViewModel(dbRepoMock, apiRepoMock).apply {
                attachNavHostController(navHostControllerMock)
                attachMainViewModel(mainViewModelMock)
            }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `round trip through search views`() =
        runTest {
            // start in entry mode by default
            assertEquals(SearchState.ENTRY, searchViewModel.searchState.value)

            // Given
            val searchResults =
                listOf(
                    forSearch(title = "One is the loneliest number"),
                    forSearch(title = "Two tickets to paradise"),
                    forSearch(title = "Three times a lady"),
                )
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(LoadingAMovie.fromList(searchResults))

            searchViewModel.updateSearchQuery(SearchQuery(title = "Forever young"))
            searchViewModel.startSearch()
            assertEquals(SearchState.RESULTS, searchViewModel.searchState.value)
            coEvery { apiRepoMock.getMovieDetails(any()) } returns
                flowOf(LoadingAMovie.Single(forCard(title = "Three times a lady")))
            searchViewModel.fetchFromRemote(1)

            // When: roundTrip
            searchViewModel.switchToSearchEntry()
            assertEquals(SearchState.ENTRY, searchViewModel.searchState.value)
            searchViewModel.switchToSearchResults()
            assertEquals(SearchState.RESULTS, searchViewModel.searchState.value)
            searchViewModel.switchToChoiceScreen()
            assertEquals(SearchState.CHOICE, searchViewModel.searchState.value)
            searchViewModel.switchToSearchEntry()
            assertEquals(SearchState.ENTRY, searchViewModel.searchState.value)
        }

    @Test
    fun `update and fetch search query in memory()`() =
        runTest {
            // Given
            val searchQuery = SearchQuery(title = "Once upon a time", creationTime = 1000)

            // When
            searchViewModel.updateSearchQuery(searchQuery)
            val result = searchViewModel.currentQuery.value

            // Then
            assertEquals(searchQuery, result)
        }

    @Test
    @Ignore("to be implemented later")
    fun `store search query in database`() {
        // Given
        val searchQuery = SearchQuery(title = "Once upon a time", creationTime = 1000)
        searchViewModel.updateSearchQuery(searchQuery)

        // When
        searchViewModel.onSaveQueryAction()

        // Then
        // TODO
    }

    @Test
    fun `search and find a movie`() =
        runTest {
            // Given
            val foundMovie =
                LoadingAMovie.Single(forSearch(tmdbId = 1007, title = "From Russia with Love"))
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(foundMovie)

            val detailMovie =
                LoadingAMovie.Single(forCard(tmdbId = 1007, title = "From Russia with Love"))
            coEvery { apiRepoMock.getMovieDetails(any()) } returns
                flowOf(detailMovie)
            searchViewModel.updateSearchQuery(SearchQuery(title = "From Russia with Love"))

            // When
            searchViewModel.startSearch()

            // Then
            assertEquals(detailMovie, searchViewModel.selectedMovie.value)
        }

    @Test
    fun `search and find multiple movies`() =
        runTest {
            // Given
            val loadingMovies =
                LoadingAMovie.Multiple(
                    listOf(
                        forSearch(title = "Live and let die"),
                        forSearch(title = "Moonraker"),
                        forSearch(title = "Octopussy"),
                    ),
                )
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(loadingMovies)
            searchViewModel.updateSearchQuery(SearchQuery(title = "Bond"))

            // When
            searchViewModel.startSearch()

            // Then
            assertEquals(loadingMovies, searchViewModel.searchResults.value)
        }

    @Test
    fun `search and find nothing`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(LoadingAMovie.Empty)
            searchViewModel.updateSearchQuery(SearchQuery(title = "nothing"))

            // When
            searchViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.SearchEmpty>()) }
        }

    @Test
    fun `search and get an error`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(LoadingAMovie.Failed(Exception()))
            searchViewModel.updateSearchQuery(SearchQuery(title = "failure"))

            // When
            searchViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.ConnectionFailed>()) }
        }

    @Test
    fun `call search without anything to search`() =
        runTest {
            // Given
            searchViewModel.updateSearchQuery(SearchQuery(title = ""))

            // When
            searchViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.EmptyTitle>()) }
        }

    @Test
    fun `display busy state while searching`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(LoadingAMovie.Loading)
            searchViewModel.updateSearchQuery(SearchQuery(title = "anything"))

            // When
            searchViewModel.startSearch()

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.Searching>()) }
        }

    @Test
    fun `cancel ongoing search`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(LoadingAMovie.Loading)
            searchViewModel.updateSearchQuery(SearchQuery(title = "stuck"))
            searchViewModel.startSearch()

            clearMocks(mainViewModelMock)
            coVerify(exactly = 0) { mainViewModelMock.closePopupOfType(PopupInfo.Searching::class) }

            // When
            searchViewModel.cancelSearch()

            // Then
            coVerify { mainViewModelMock.closePopupOfType(PopupInfo.Searching::class) }
        }

    @Test
    fun `fetch movie details from remote`() =
        runTest {
            // Given
            val tmdbId = 1007L
            val returnedMovie =
                LoadingAMovie.Single(
                    forCard(
                        id = 1007,
                        tmdbId = tmdbId,
                        title = "From Russia with Love",
                        originalTitle = "Shaken, not stirred",
                        year = 1963,
                        thumbnailUrl = "bond.jpg",
                        coverUrl = "james.jpg",
                        genres = listOf("action"),
                        imdbId = "tt007",
                        tagline = "with license to...",
                        plot = "Cold War Shenanigans",
                        runtimeMinutes = 115,
                    ),
                )
            coEvery { apiRepoMock.getMovieDetails(tmdbId) } returns
                flowOf(returnedMovie)

            // When
            searchViewModel.fetchFromRemote(tmdbId)

            // Then
            assertEquals(returnedMovie, searchViewModel.selectedMovie.value)
        }

    @Test
    fun `fetch movie details failed`() =
        runTest {
            // Given
            coEvery { apiRepoMock.getMovieDetails(any()) } returns
                flowOf(LoadingAMovie.Failed(Exception()))

            // When
            searchViewModel.fetchFromRemote(1)

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.ConnectionFailed>()) }
        }

    @Test
    fun `fetch movie details brought nothing`() =
        runTest {
            // Given
            coEvery { apiRepoMock.getMovieDetails(any()) } returns
                flowOf(LoadingAMovie.Empty)

            // When
            searchViewModel.fetchFromRemote(1)

            // Then
            coVerify { mainViewModelMock.showPopup(any<PopupInfo.SearchEmpty>()) }
        }

    @Test
    fun `save movie and quit`() =
        runTest {
            // Given
            val movieToSave = forCard(title = "saved")
            coEvery { apiRepoMock.getMovieDetails(any()) } returns
                flowOf(LoadingAMovie.Single(movieToSave))
            searchViewModel.fetchFromRemote(1)

            // When
            searchViewModel.onSaveMovieAction()

            // Then
            coVerify { dbRepoMock.storeMovie(movieToSave) }
            coVerify {
                navHostControllerMock.navigate(
                    Routes.HOME.route,
                    any<NavOptionsBuilder.() -> Unit>(),
                )
            }
        }

    @Test
    fun `back button on Entry leaves the screen`() {
        // Given
        searchViewModel.switchToSearchEntry()

        // When
        searchViewModel.handleBackButton()

        // Then
        coVerify {
            navHostControllerMock.navigate(
                Routes.HOME.route,
                any<NavOptionsBuilder.() -> Unit>(),
            )
        }
    }

    @Test
    fun `back button close search results`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(
                    LoadingAMovie.fromList(
                        listOf(
                            forSearch(title = "ABC"),
                            forSearch(title = "CDE"),
                        ),
                    ),
                )
            searchViewModel.updateSearchQuery(SearchQuery(title = "C"))
            searchViewModel.startSearch()
            searchViewModel.switchToSearchResults()

            // When
            searchViewModel.handleBackButton()

            // Then
            assertEquals(SearchState.ENTRY, searchViewModel.searchState.value)
        }

    @Test
    fun `back button close choice screen back to search results`() =
        runTest {
            // Given
            coEvery { apiRepoMock.getMovieDetails(any()) } returns
                flowOf(LoadingAMovie.Single(forCard(title = "ABC")))
            searchViewModel.fetchFromRemote(1)
            assertEquals(SearchState.CHOICE, searchViewModel.searchState.value)

            // When
            searchViewModel.handleBackButton()

            // Then
            assertEquals(SearchState.ENTRY, searchViewModel.searchState.value)
        }
}
