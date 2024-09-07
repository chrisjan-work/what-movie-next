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

import com.lairofpixies.whatmovienext.BuildConfig
import com.lairofpixies.whatmovienext.models.network.RequestInterceptorFactory.USER_AGENT
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class TmdbApiTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var tmdbApi: TmdbApi

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `find some movies`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ 
                            "results" : [ { "id": 1, "title": "example title" }, { "id": 3, "title": "example title 2: the revenge" } ], 
                            "page": 2, "total_pages": 5 }
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = tmdbApi.findMoviesByTitle("example title", language = "en")

            // Then
            val expectedMovies =
                listOf(
                    TmdbMovieBasic(tmdbId = 1, title = "example title"),
                    TmdbMovieBasic(tmdbId = 3, title = "example title 2: the revenge"),
                )
            assertEquals(expectedMovies, result.results)
            assertEquals(2, result.page)
            assertEquals(5, result.totalPages)
        }

    @Test
    fun `retrieve config`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ "images" : { "secure_base_url": "example.com", "poster_sizes": ["w92", "w154", "w780"], "profile_sizes": ["w45", "w185"] } }""",
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = tmdbApi.getConfiguration()

            // Then
            assertEquals("example.com", result.images.url)
            assertEquals(listOf("w92", "w154", "w780"), result.images.posterSizes)
            assertEquals(listOf("w45", "w185"), result.images.profileSizes)
        }

    @Test
    fun `request details for wrong movie id`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ 
                             "success" : false,
                             "status_code": 100,
                             "status_message":"Movie not found"
                        }
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = tmdbApi.getMovieDetails(tmdbId = 1, language = "en")

            // Then
            assertEquals(TmdbMovieExtended(success = false), result)
        }

    @Test
    fun `request details for valid movie id`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ 
                            "id": 1,
                            "imdb_id": "tt100",
                            "title": "Terminator 2",
                            "original_title": "Terminator 2",
                            "poster_path": "/terminator2.jpg",
                            "release_date": "1991-10-24",
                            "tagline": "Hasta la vista, baby.",
                            "overview": "robots from the future",
                            "runtime": 137,
                            "genres": [ { "id": 188, "name": "Action" } ],
                            "credits": { 
                                "cast": [{
                                    "id": 2000,
                                    "name": "Solsonegene",
                                    "original_name": "Arnol Solsonegene",
                                    "profile_path": "/solsonesonegene.jpg",
                                    "character": "the good terminator",
                                    "order": 1
                                }], 
                                "crew": [{
                                    "id": 3000,
                                    "name": "Cameron",
                                    "original_name": "James Cameron",
                                    "profile_path": "/titanic.jpg",
                                    "job": "director",
                                    "department": "Directing"
                                }] 
                            } }
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = tmdbApi.getMovieDetails(tmdbId = 1, language = "en")

            // Then
            val expectedMovie =
                TmdbMovieExtended(
                    tmdbId = 1,
                    imdbId = "tt100",
                    title = "Terminator 2",
                    originalTitle = "Terminator 2",
                    posterPath = "/terminator2.jpg",
                    releaseDate = "1991-10-24",
                    tagline = "Hasta la vista, baby.",
                    summary = "robots from the future",
                    runtime = 137,
                    genres = listOf(TmdbGenres.TmdbGenre(tmdbId = 188, name = "Action")),
                    credits =
                        TmdbMovieExtended.TmdbCredits(
                            cast =
                                listOf(
                                    TmdbMovieExtended.TmdbCastMember(
                                        tmdbId = 2000,
                                        name = "Solsonegene",
                                        originalName = "Arnol Solsonegene",
                                        profilePath = "/solsonesonegene.jpg",
                                        character = "the good terminator",
                                        order = 1,
                                    ),
                                ),
                            crew =
                                listOf(
                                    TmdbMovieExtended.TmdbCrewMember(
                                        tmdbId = 3000,
                                        name = "Cameron",
                                        originalName = "James Cameron",
                                        profilePath = "/titanic.jpg",
                                        job = "director",
                                        department = "Directing",
                                    ),
                                ),
                        ),
                )
            assertEquals(expectedMovie, result)
        }

    @Test
    fun `tmdb interceptor`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody("{}")
            mockWebServer.enqueue(mockResponse)

            // When
            tmdbApi.getMovieDetails(tmdbId = 1, language = "en")
            val request = mockWebServer.takeRequest()

            // Then
            assertEquals(USER_AGENT, request.getHeader("User-Agent"))
            assertEquals("Bearer ${BuildConfig.tmdbtoken}", request.getHeader("Authorization"))
        }
}
