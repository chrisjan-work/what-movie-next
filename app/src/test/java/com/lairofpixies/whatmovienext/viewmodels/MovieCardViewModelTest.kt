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

import com.lairofpixies.whatmovienext.models.data.LoadingMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieCardViewModelTest {
    private lateinit var cardViewModel: MovieCardViewModel
    private lateinit var repo: MovieRepository

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        cardViewModel = MovieCardViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `no movie`() {
        // Given

        // When
        cardViewModel.startFetchingMovie(null)
        val result = cardViewModel.currentMovie.value

        // Then
        assertEquals(LoadingMovie.Empty, result)
    }

    @Test
    fun `get single movie`() {
        // Given
        val partialMovie =
            LoadingMovie.Single(
                Movie(id = 10, title = "single movie"),
            )
        every { repo.singleMovie(10) } returns
            MutableStateFlow(partialMovie).asStateFlow()

        // When
        cardViewModel.startFetchingMovie(10)
        val result = cardViewModel.currentMovie.value

        // Then
        assertEquals(partialMovie, result)
    }

    @Test
    fun `update watch state of movie`() {
        // When
        cardViewModel.updateMovieWatched(10, WatchState.WATCHED)
        cardViewModel.updateMovieWatched(31, WatchState.PENDING)

        // Then
        coVerifyOrder {
            repo.setWatchState(10, WatchState.WATCHED)
            repo.setWatchState(31, WatchState.PENDING)
        }
    }
}
