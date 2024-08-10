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
class WikidataApiTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var wikidataApi: WikidataApi

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
                          "head" : {
                            "vars" : [ "entity", "rottentomatoes_id", "rottentomatoes_rating", "metacritic_id", "metacritic_rating" ]
                          },
                          "results" : {
                            "bindings" : [ {
                              "entity" : {
                                "type" : "uri",
                                "value" : "http://www.wikidata.org/entity/Q24278982"
                              },
                              "rottentomatoes_id" : {
                                "type" : "literal",
                                "value" : "m/indiana_jones_and_the_dial_of_destiny"
                              },
                              "rottentomatoes_rating" : {
                                "type" : "literal",
                                "value" : "70%"
                              },
                              "metacritic_id" : {
                                "type" : "literal",
                                "value" : "movie/indiana-jones-and-the-dial-of-destiny"
                              },
                              "metacritic_rating" : {
                                "type" : "literal",
                                "value" : "58/100"
                              }
                            } ]
                          }
                        }
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = wikidataApi.askSparql("blabla")

            // Then
            assertEquals("http://www.wikidata.org/entity/Q24278982", result.entity)
            assertEquals("m/indiana_jones_and_the_dial_of_destiny", result.rottenTomatoesId)
            assertEquals("70%", result.rottenTomatoesRating)
            assertEquals("movie/indiana-jones-and-the-dial-of-destiny", result.metacriticId)
            assertEquals("58/100", result.metacriticRating)
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
                          "head" : {
                            "vars" : [ "entity", "rottentomatoes_id", "rottentomatoes_rating", "metacritic_id", "metacritic_rating" ]
                          },
                          "results" : {
                            "bindings" : []
                          }
                        }
                        """.trimMargin(),
                    )
            mockWebServer.enqueue(mockResponse)

            // When
            val result = wikidataApi.askSparql("something")

            // Then
            assertEquals("", result.entity)
            assertEquals("", result.rottenTomatoesId)
            assertEquals("", result.rottenTomatoesRating)
            assertEquals("", result.metacriticId)
            assertEquals("", result.metacriticRating)
        }
}
