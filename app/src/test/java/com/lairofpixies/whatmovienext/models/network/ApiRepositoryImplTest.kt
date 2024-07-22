package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.BackendMovie
import com.lairofpixies.whatmovienext.models.data.DownloadMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
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
    fun `launch find movies by title, default state is loading`() =
        runTest {
            // Given
            coEvery { movieApi.findMoviesByTitle(any()) } coAnswers {
                suspendCancellableCoroutine {}
            }

            // When
            val result = sut.findMoviesByTitle("test").first()

            // Then
            assertEquals(DownloadMovieInfo.Loading, result)
        }

    @Test
    fun `find movies by title, none available`() =
        runTest {
            // Given
            coEvery { movieApi.findMoviesByTitle(any()) } returns emptyList()

            // When
            val result = sut.findMoviesByTitle("test").first()

            // Then
            assertEquals(DownloadMovieInfo.Empty, result)
        }

    @Test
    fun `find movies by title, one available`() =
        runTest {
            // Given
            coEvery { movieApi.findMoviesByTitle(any()) } returns
                listOf(
                    BackendMovie(title = "test"),
                )

            // When
            val result = sut.findMoviesByTitle("test").first()

            // Then
            assertEquals(DownloadMovieInfo.Single(Movie(title = "test")), result)
        }

    @Test
    fun `find movies by title, three available`() =
        runTest {
            // Given
            val receivedMovies =
                listOf(
                    BackendMovie(title = "movie1"),
                    BackendMovie(title = "movie2"),
                    BackendMovie(title = "movie3"),
                )
            coEvery { movieApi.findMoviesByTitle(any()) } returns receivedMovies

            // When
            val result = sut.findMoviesByTitle("test").first()

            // Then
            val expectedMovies =
                listOf(
                    Movie(title = "movie1"),
                    Movie(title = "movie2"),
                    Movie(title = "movie3"),
                )
            assertEquals(DownloadMovieInfo.Multiple(expectedMovies), result)
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
            val result = sut.findMoviesByTitle("test").first()

            // Then
            assertEquals(DownloadMovieInfo.Failed(http404), result)
        }
}
