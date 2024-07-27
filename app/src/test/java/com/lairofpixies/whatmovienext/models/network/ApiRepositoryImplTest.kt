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
package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.RemoteMovie
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ApiRepositoryImplTest {
    private lateinit var movieApi: MovieApi
    private lateinit var sut: ApiRepository

    @Before
    fun setUp() {
        movieApi = mockk(relaxed = true)
        sut = ApiRepositoryImpl(movieApi, UnconfinedTestDispatcher())
    }

    @Test
    fun `find movies by title, none available`() =
        runTest {
            // Given
            coEvery { movieApi.findMoviesByTitle(any()) } returns emptyList()

            // When
            val result = sut.findMoviesByTitle("test").value

            // Then
            assertEquals(AsyncMovieInfo.Empty, result)
        }

    @Test
    fun `find movies by title, one available`() =
        runTest {
            // Given
            coEvery { movieApi.findMoviesByTitle(any()) } returns
                listOf(
                    RemoteMovie(title = "test"),
                )

            // When
            val result = sut.findMoviesByTitle("test").value

            // Then
            assertEquals(AsyncMovieInfo.Single(Movie(title = "test")), result)
        }

    @Test
    fun `find movies by title, three available`() =
        runTest {
            // Given
            val receivedMovies =
                listOf(
                    RemoteMovie(title = "movie1"),
                    RemoteMovie(title = "movie2"),
                    RemoteMovie(title = "movie3"),
                )
            coEvery { movieApi.findMoviesByTitle(any()) } returns receivedMovies

            // When
            val result = sut.findMoviesByTitle("test").value

            // Then
            val expectedMovies =
                listOf(
                    Movie(title = "movie1"),
                    Movie(title = "movie2"),
                    Movie(title = "movie3"),
                )
            assertEquals(AsyncMovieInfo.Multiple(expectedMovies), result)
        }

    @Test
    fun `find movies by title, connection fails`() =
        runTest {
            // Given
            val http404 =
                HttpException(
                    Response.error<Any>(
                        404,
                        ResponseBody.create(null, ""),
                    ),
                )
            coEvery { movieApi.findMoviesByTitle(any()) } throws http404

            // When
            val result = sut.findMoviesByTitle("test").value

            // Then
            assertEquals(AsyncMovieInfo.Failed(http404), result)
        }
}
