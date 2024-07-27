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

import com.lairofpixies.whatmovienext.models.data.RemoteMovieSummary
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
class MovieApiTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var movieApi: MovieApi

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
            val result = movieApi.findMoviesByTitle("example title")

            // Then
            val expectedMovies =
                listOf(
                    RemoteMovieSummary(tmdbId = 1, title = "example title"),
                    RemoteMovieSummary(tmdbId = 2, title = "example title 2: the revenge"),
                )
            assertEquals(expectedMovies, result.results)
        }
}
