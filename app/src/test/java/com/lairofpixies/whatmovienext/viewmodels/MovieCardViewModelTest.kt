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

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.TestMovie.forCard
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.views.navigation.Routes
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class MovieCardViewModelTest {
    private lateinit var repo: MovieRepository
    private lateinit var mainViewModelMock: MainViewModel
    private lateinit var cardViewModel: MovieCardViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        mainViewModelMock = mockk(relaxed = true)

        cardViewModel = MovieCardViewModel(repo)
        cardViewModel.attachMainViewModel(mainViewModelMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `no movie`() =
        runTest {
            // Given

            // When
            cardViewModel.startFetchingMovie(null)
            val result = cardViewModel.currentMovie.value

            // Then
            assertEquals(AsyncMovie.Empty, result)
        }

    @Test
    fun `get single movie`() {
        // Given
        val partialMovie =
            AsyncMovie.Single(
                forCard(id = 10, title = "single movie"),
            )
        every { repo.singleCardMovie(10) } returns
            flowOf(partialMovie)

        // When
        cardViewModel.startFetchingMovie(10)
        val result = cardViewModel.currentMovie.value

        // Then
        assertEquals(partialMovie, result)
    }

    @Test
    fun `update watch state of movie`() {
        // When
        cardViewModel.updateMovieWatchDates(10, listOf(678L))
        cardViewModel.updateMovieWatchDates(31, emptyList())

        // Then
        coVerifyOrder {
            repo.updateWatchDates(10, listOf(678L))
            repo.updateWatchDates(31, emptyList())
        }
    }

    @Test
    fun `archive currently viewed movie`() =
        runTest {
            // Given
            // Given
            val partialMovie =
                AsyncMovie.Single(
                    forCard(id = 10, title = "single movie"),
                )
            every { repo.singleCardMovie(10) } returns
                flowOf(partialMovie)
            cardViewModel.startFetchingMovie(10)

            // When
            cardViewModel.archiveCurrentMovie()

            // Then
            coVerify { repo.archiveMovie(10) }
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
            cardViewModel.randomizer = Random(100)
            assertEquals(true, cardViewModel.canSpinRoulette())

            // randomizer with fixed seed will produce fixed sequence
            val expectedSequence: List<Long> = listOf(3, 2, 3, 1, 3, 2)
            var lastId = 0L

            expectedSequence.forEach { expectedId ->
                clearMocks(mainViewModelMock, answers = false, recordedCalls = true)
                // When
                cardViewModel.onNavigateToRandomMovie(lastId)
                lastId = expectedId

                // Then
                verify {
                    mainViewModelMock.onNavigateWithParam(
                        Routes.SingleMovieView,
                        expectedId,
                        popToHome = true,
                    )
                }
            }
        }

    @Test
    fun `roulette feature disabled with only 1 movie`() =
        runTest {
            // Given
            every { mainViewModelMock.listedMovies } returns
                MutableStateFlow(
                    AsyncMovie.Single(
                        forList(id = 2, title = "Pitch Black"),
                    ),
                )
            assertEquals(false, cardViewModel.canSpinRoulette())
        }

    @Test
    fun `create share link`() =
        runTest {
            // Given
            val partialMovie =
                AsyncMovie.Single(
                    forCard(id = 10, title = "single movie", tmdbId = 111L),
                )
            every { repo.singleCardMovie(10) } returns
                flowOf(partialMovie)

            // When
            cardViewModel.startFetchingMovie(10)
            val result = cardViewModel.shareableLink()

            // Then
            assertEquals("whatmovienext://movie/111", result)
        }

    @Test
    fun `can share`() =
        runTest {
            // Given
            val partialMovie =
                AsyncMovie.Single(
                    forCard(id = 10, title = "single movie", tmdbId = 111L),
                )
            every { repo.singleCardMovie(10) } returns flowOf(partialMovie)

            // When
            val notShareable = cardViewModel.canShare()
            cardViewModel.startFetchingMovie(10)
            val shareable = cardViewModel.canShare()

            // Then
            assertEquals(false, notShareable)
            assertEquals(true, shareable)
        }
}
