package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.BackendMovie
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
                    .setBody("[ { \"title\": \"example title\" }, { \"title\": \"example title\" } ]")
            mockWebServer.enqueue(mockResponse)

            // When
            val result = movieApi.findMoviesByTitle("example title")

            // Then
            val expectedMovies =
                listOf(
                    BackendMovie("example title"),
                    BackendMovie("example title"),
                )
            assertEquals(expectedMovies, result)
        }
}
