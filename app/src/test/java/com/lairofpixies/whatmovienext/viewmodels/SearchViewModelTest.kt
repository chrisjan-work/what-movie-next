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

import androidx.compose.foundation.lazy.LazyListState
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.PagedMovies
import com.lairofpixies.whatmovienext.models.data.SearchQuery
import com.lairofpixies.whatmovienext.models.data.TestMovie.forCard
import com.lairofpixies.whatmovienext.models.data.TestMovie.forSearch
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
            coEvery { apiRepoMock.findMoviesByTitle(any(), null) } returns
                flowOf(PagedMovies.fromList(searchResults))

            searchViewModel.updateSearchQuery(SearchQuery(title = "Forever young"))
            searchViewModel.startSearch()
            assertEquals(SearchState.RESULTS, searchViewModel.searchState.value)
            coEvery { apiRepoMock.getMovieDetails(any()) } returns
                flowOf(AsyncMovie.Single(forCard(title = "Three times a lady")))
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
    fun `search and find a movie`() =
        runTest {
            // Given
            val foundMovie = forSearch(tmdbId = 1007, title = "From Russia with Love")
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(PagedMovies(AsyncMovie.Single(foundMovie)))

            val detailMovie =
                AsyncMovie.Single(forCard(tmdbId = 1007, title = "From Russia with Love"))
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
            val returnedMovies =
                listOf(
                    forSearch(title = "Live and let die"),
                    forSearch(title = "Moonraker"),
                    forSearch(title = "Octopussy"),
                )
            val pagedMovies = PagedMovies.fromList(returnedMovies)
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(pagedMovies)
            searchViewModel.updateSearchQuery(SearchQuery(title = "Bond"))

            // When
            searchViewModel.startSearch()

            // Then
            val result = searchViewModel.searchResults.value
            assertEquals(pagedMovies, result)
        }

    @Test
    fun `search and find multiple movies in two pages`() =
        runTest {
            // Given
            val movies =
                listOf(
                    forSearch(title = "Live and let die"),
                    forSearch(title = "Moonraker"),
                    forSearch(title = "Octopussy"),
                )
            val firstPage =
                PagedMovies(
                    movies =
                        AsyncMovie.Multiple(movies.take(2)),
                    lastPage = 1,
                    pagesLeft = 1,
                )
            val secondPage =
                PagedMovies(
                    movies =
                        AsyncMovie.Single(movies.last()),
                    lastPage = 2,
                    pagesLeft = 0,
                )
            coEvery { apiRepoMock.findMoviesByTitle(any(), any()) } returnsMany
                listOf(
                    flowOf(firstPage),
                    flowOf(secondPage),
                )
            searchViewModel.updateSearchQuery(SearchQuery(title = "Bond"))

            // When
            searchViewModel.startSearch()
            searchViewModel.continueSearch()
            val result = searchViewModel.searchResults.value

            // Then
            val expected =
                PagedMovies(
                    movies = AsyncMovie.Multiple(movies),
                    lastPage = 2,
                    pagesLeft = 0,
                )
            assertEquals(expected, result)
        }

    @Test
    fun `with multiple movies, scroll, select, and go back to results`() =
        runTest {
            // Given
            val returnedMovies =
                listOf(
                    forSearch(title = "Live and let die"),
                    forSearch(title = "Moonraker"),
                    forSearch(title = "Octopussy"),
                )
            val pagedMovies = PagedMovies.fromList(returnedMovies)
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(pagedMovies)
            searchViewModel.updateSearchQuery(SearchQuery(title = "Bond"))
            searchViewModel.startSearch()

            // When scroll, select result, go back
            searchViewModel.resultsScroll = LazyListState(firstVisibleItemIndex = 10)
            searchViewModel.fetchFromRemote(returnedMovies[1].searchData.tmdbId)
            searchViewModel.switchToSearchResults()

            // Then
            assertEquals(10, searchViewModel.resultsScroll.firstVisibleItemIndex)
        }

    @Test
    fun `search and find nothing`() =
        runTest {
            // Given
            coEvery { apiRepoMock.findMoviesByTitle(any()) } returns
                flowOf(PagedMovies.Empty)
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
                flowOf(PagedMovies.Failed(Exception()))
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
                flowOf(PagedMovies.Loading)
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
                flowOf(PagedMovies.Loading)
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
                AsyncMovie.Single(
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
                flowOf(AsyncMovie.Failed(Exception()))

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
                flowOf(AsyncMovie.Empty)

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
                flowOf(AsyncMovie.Single(movieToSave))
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
                    PagedMovies.fromList(
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
                flowOf(AsyncMovie.Single(forCard(title = "ABC")))
            searchViewModel.fetchFromRemote(1)
            assertEquals(SearchState.CHOICE, searchViewModel.searchState.value)

            // When
            searchViewModel.handleBackButton()

            // Then
            assertEquals(SearchState.ENTRY, searchViewModel.searchState.value)
        }
}
