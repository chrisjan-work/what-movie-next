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
class OmdbApiTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var omdbApi: OmdbApi

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetch movie ratings success`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ 
                            "Response" : "True", 
                            "Ratings": [
                                {
                                    "Source": "Decimal database",
                                    "Value": "6.1/10"
                                },
                                {
                                    "Source": "Percent database",
                                    "Value": "90%"
                                }
                            ]
                            }
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = omdbApi.fetchMovieRatings("tt10")

            // Then
            assertEquals("True", result.success)
            assertEquals("Decimal database", result.ratings[0].source)
            assertEquals("6.1/10", result.ratings[0].value)
            assertEquals("Percent database", result.ratings[1].source)
            assertEquals("90%", result.ratings[1].value)
        }

    @Test
    fun `fetch movie ratings failure`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{ 
                            "Response" : "False", 
                            "Error": "Simulated error"
                            }
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = omdbApi.fetchMovieRatings("tt10")

            // Then
            assertEquals("False", result.success)
            assertEquals("Simulated error", result.errorMessage)
        }

    @Test
    fun `omdb interceptor`() =
        runTest {
            // Given
            val mockResponse =
                MockResponse()
                    .setResponseCode(200)
                    .setBody("{}")
            mockWebServer.enqueue(mockResponse)

            // When
            omdbApi.fetchMovieRatings("tt10")
            val request = mockWebServer.takeRequest()

            // Then
            assertEquals(RequestInterceptorFactory.USER_AGENT, request.getHeader("User-Agent"))
            assertEquals(BuildConfig.omdbkey, request.requestUrl?.queryParameter("apikey"))
        }
}
