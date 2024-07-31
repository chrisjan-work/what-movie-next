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

import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
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
                        """{ "results" : [ { "id": 1, "title": "example title" }, { "id": 3, "title": "example title 2: the revenge" } ] }""",
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = tmdbApi.findMoviesByTitle("example title")

            // Then
            val expectedMovies =
                listOf(
                    TmdbMovieBasic(tmdbId = 1, title = "example title"),
                    TmdbMovieBasic(tmdbId = 3, title = "example title 2: the revenge"),
                )
            assertEquals(expectedMovies, result.results)
        }

    @Test
    fun `retrieve config`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ "images" : { "secure_base_url": "example.com", "poster_sizes": ["w92", "w154", "w780"] } }""",
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = tmdbApi.getConfiguration()

            // Then
            val expectedSizes = listOf("w92", "w154", "w780")
            assertEquals("example.com", result.images.url)
            assertEquals(expectedSizes, result.images.sizes)
        }

    @Test
    fun `retrieve genres`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ "genres" : [
                            { "id": 188, "name": "Radio Gugu" },
                            { "id": 288, "name": "Radio Blabla" }
                            ]}
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = tmdbApi.getGenres()

            // Then
            val expectedGenres =
                listOf(
                    TmdbGenres.TmdbGenre(tmdbId = 188, name = "Radio Gugu"),
                    TmdbGenres.TmdbGenre(tmdbId = 288, name = "Radio Blabla"),
                )
            assertEquals(expectedGenres, result.genres)
        }
}
