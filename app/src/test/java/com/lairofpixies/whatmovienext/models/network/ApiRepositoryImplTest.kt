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

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.TestMovie
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.mappers.RemoteMapper
import com.lairofpixies.whatmovienext.models.mappers.testCardMovieExtended
import com.lairofpixies.whatmovienext.models.mappers.testOmdbMovieRatings
import com.lairofpixies.whatmovienext.models.mappers.testTmdbMovieExtended
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import com.lairofpixies.whatmovienext.models.network.data.TmdbSearchResults
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ApiRepositoryImplTest {
    private lateinit var tmdbApi: TmdbApi
    private lateinit var omdbApi: OmdbApi
    private lateinit var wikidataApi: WikidataApi
    private lateinit var configRepo: ConfigRepository
    private lateinit var genreRepository: GenreRepository
    private lateinit var remoteMapper: RemoteMapper
    private lateinit var apiRepository: ApiRepository

    @Before
    fun setUp() {
        tmdbApi = mockk(relaxed = true)
        omdbApi = mockk(relaxed = true)
        wikidataApi = mockk(relaxed = true)
        configRepo = mockk(relaxed = true)
        genreRepository = mockk(relaxed = true)
        remoteMapper = RemoteMapper(configRepo, genreRepository)
    }

    private fun TestScope.initializeSut() {
        apiRepository =
            ApiRepositoryImpl(
                tmdbApi,
                omdbApi,
                wikidataApi,
                remoteMapper,
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            )
        advanceUntilIdle()
    }

    @Test
    fun `find movies by title, none available`() =
        runTest {
            // Given
            coEvery { tmdbApi.findMoviesByTitle(any()) } returns TmdbSearchResults(results = emptyList())
            initializeSut()

            // When
            val result = apiRepository.findMoviesByTitle("test").last().movies

            // Then
            assertEquals(AsyncMovie.Empty, result)
        }

    @Test
    fun `find movies by title, one available`() =
        runTest {
            // Given
            coEvery { tmdbApi.findMoviesByTitle(any()) } returns
                TmdbSearchResults(
                    results =
                        listOf(
                            TmdbMovieBasic(tmdbId = 1, title = "test"),
                        ),
                )
            initializeSut()

            // When
            val result = apiRepository.findMoviesByTitle("test").last()

            // Then
            val expectedMovie =
                AsyncMovie.Single(TestMovie.forSearch(title = "test", tmdbId = 1))
            assertEquals(expectedMovie, result.movies)
        }

    @Test
    fun `find movies by title, three available`() =
        runTest {
            // Given
            val receivedMovies =
                listOf(
                    TmdbMovieBasic(tmdbId = 1, title = "movie1"),
                    TmdbMovieBasic(tmdbId = 2, title = "movie2"),
                    TmdbMovieBasic(tmdbId = 3, title = "movie3"),
                )
            coEvery { tmdbApi.findMoviesByTitle(any()) } returns
                TmdbSearchResults(results = receivedMovies)
            initializeSut()

            // When
            val result = apiRepository.findMoviesByTitle("test").last()

            // Then
            val expectedMovies =
                listOf(
                    TestMovie.forSearch(tmdbId = 1, title = "movie1"),
                    TestMovie.forSearch(tmdbId = 2, title = "movie2"),
                    TestMovie.forSearch(tmdbId = 3, title = "movie3"),
                )
            assertEquals(AsyncMovie.Multiple(expectedMovies), result.movies)
        }

    @Test
    fun `find movies by title, count pages`() =
        runTest {
            // Given
            val receivedMovies =
                listOf(
                    TmdbMovieBasic(tmdbId = 1, title = "movie1"),
                    TmdbMovieBasic(tmdbId = 2, title = "movie2"),
                    TmdbMovieBasic(tmdbId = 3, title = "movie3"),
                )
            coEvery { tmdbApi.findMoviesByTitle(any()) } returns
                TmdbSearchResults(results = receivedMovies, page = 3, totalPages = 7)
            initializeSut()

            // When
            val result = apiRepository.findMoviesByTitle("test").last()

            // Then
            val expectedMovies =
                listOf(
                    TestMovie.forSearch(tmdbId = 1, title = "movie1"),
                    TestMovie.forSearch(tmdbId = 2, title = "movie2"),
                    TestMovie.forSearch(tmdbId = 3, title = "movie3"),
                )
            assertEquals(AsyncMovie.Multiple(expectedMovies), result.movies)
            assertEquals(3, result.lastPage)
            assertEquals(4, result.pagesLeft)
        }

    @Test
    fun `find movies by title, connection fails`() =
        runTest {
            // Given
            val http404 =
                HttpException(
                    Response.error<Any>(
                        404,
                        "".toResponseBody(null),
                    ),
                )
            coEvery { tmdbApi.findMoviesByTitle(any()) } throws http404
            initializeSut()

            // When
            val result = apiRepository.findMoviesByTitle("test").last()

            // Then
            assertEquals(AsyncMovie.Failed(http404), result.movies)
        }

    @Test
    fun `get movie details`() =
        runTest {
            // Given
            coEvery { tmdbApi.getMovieDetails(any()) } returns testTmdbMovieExtended()
            coEvery { omdbApi.fetchMovieRatings(any()) } returns testOmdbMovieRatings()
            remoteMapper =
                mockk(relaxed = true) {
                    every { toCardMovie(any<TmdbMovieExtended>(), any()) } returns testCardMovieExtended()
                }
            initializeSut()

            // When
            val result = apiRepository.getMovieDetails(99).last()

            // Then
            assertEquals(AsyncMovie.Single(testCardMovieExtended()), result)
        }

    @Test
    fun `get movie details, server error`() =
        runTest {
            // Given
            coEvery { tmdbApi.getMovieDetails(any()) } returns TmdbMovieExtended(success = false)
            initializeSut()

            // When
            val result = apiRepository.getMovieDetails(99).last()

            // Then
            assertEquals(AsyncMovie.Failed::class, result::class)
        }
}
