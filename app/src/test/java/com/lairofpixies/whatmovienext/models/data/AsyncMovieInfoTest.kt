package com.lairofpixies.whatmovienext.models.data

import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AsyncMovieInfoTest {
    @Test
    fun `from list when list is empty`() {
        val asyncMovieInfo = AsyncMovieInfo.fromList(emptyList())
        assertEquals(
            AsyncMovieInfo.Empty,
            asyncMovieInfo,
        )
    }

    @Test
    fun `from list when list has one item`() {
        // Given
        val movie = Movie(title = "average movie")

        // When
        val asyncMovieInfo = AsyncMovieInfo.fromList(listOf(movie))

        // Then
        assertEquals(
            AsyncMovieInfo.Single(movie),
            asyncMovieInfo,
        )
    }

    @Test
    fun `from list when list has multiple items`() {
        // Given
        val movies =
            listOf(
                Movie(title = "interesting movie"),
                Movie(title = "boring movie"),
            )

        // When
        val asyncMovieInfo = AsyncMovieInfo.fromList(movies)

        // Then
        assertEquals(
            AsyncMovieInfo.Multiple(movies),
            asyncMovieInfo,
        )
    }

    @Test
    fun `is missing table`() {
        val isMissingPairs: List<Pair<AsyncMovieInfo?, Boolean>> =
            listOf(
                null to true,
                AsyncMovieInfo.Loading to false,
                AsyncMovieInfo.Failed(mockk()) to true,
                AsyncMovieInfo.Empty to true,
                AsyncMovieInfo.Single(mockk()) to false,
                AsyncMovieInfo.Multiple(emptyList()) to false,
            )

        isMissingPairs.forEach { (assyncMovieInfo, expectedResult) ->
            assertEquals(
                expectedResult,
                assyncMovieInfo.isMissing(),
            )
        }
    }

    @Test
    fun `has movie table`() {
        val hasMoviePairs: List<Pair<AsyncMovieInfo?, Boolean>> =
            listOf(
                null to false,
                AsyncMovieInfo.Loading to false,
                AsyncMovieInfo.Failed(mockk()) to false,
                AsyncMovieInfo.Empty to false,
                AsyncMovieInfo.Single(mockk()) to true,
                AsyncMovieInfo.Multiple(emptyList()) to true,
            )

        hasMoviePairs.forEach { (assyncMovieInfo, expectedResult) ->
            assertEquals(
                expectedResult,
                assyncMovieInfo.hasMovie(),
            )
        }
    }

    @Test
    fun `single movie to list`() {
        // Given
        val movie = Movie(title = "Romantic night")

        // When
        val movieList = AsyncMovieInfo.Single(movie).toList()

        // Then
        assertEquals(listOf(movie), movieList)
    }

    @Test
    fun `multiple movies to list`() {
        // Given
        val movies =
            listOf(
                Movie(title = "The wild bunch"),
                Movie(title = "Au Reservoir Les Oh Fun"),
            )

        // When
        val movieList = AsyncMovieInfo.Multiple(movies).toList()

        // Then
        assertEquals(movies, movieList)
    }

    @Test
    fun `empty list`() {
        val movieList = AsyncMovieInfo.Empty.toList()

        assertEquals(emptyList<Movie>(), movieList)
    }
}
