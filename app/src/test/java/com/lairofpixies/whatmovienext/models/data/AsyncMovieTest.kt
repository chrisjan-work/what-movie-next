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
package com.lairofpixies.whatmovienext.models.data

import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AsyncMovieTest {
    @Test
    fun `from list when list is empty`() {
        val asyncMovie = AsyncMovie.fromList(emptyList())
        assertEquals(
            AsyncMovie.Empty,
            asyncMovie,
        )
    }

    @Test
    fun `from list when list has one item`() {
        // Given
        val movie = TestAMovie.forSearch(title = "average movie")

        // When
        val asyncMovie = AsyncMovie.fromList(listOf(movie))

        // Then
        assertEquals(
            AsyncMovie.Single(movie),
            asyncMovie,
        )
    }

    @Test
    fun `from list when list has multiple items`() {
        // Given
        val movies =
            listOf(
                TestAMovie.forSearch(title = "interesting movie"),
                TestAMovie.forSearch(title = "boring movie"),
            )

        // When
        val asyncMovie = AsyncMovie.fromList(movies)

        // Then
        assertEquals(
            AsyncMovie.Multiple(movies),
            asyncMovie,
        )
    }

    @Test
    fun `is missing table`() {
        val isMissingPairs: List<Pair<AsyncMovie?, Boolean>> =
            listOf(
                null to true,
                AsyncMovie.Loading to false,
                AsyncMovie.Failed(mockk()) to true,
                AsyncMovie.Empty to true,
                AsyncMovie.Single(mockk<Movie.ForSearch>()) to false,
                AsyncMovie.Multiple(emptyList<Movie.ForSearch>()) to false,
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
        val hasMoviePairs: List<Pair<AsyncMovie?, Boolean>> =
            listOf(
                null to false,
                AsyncMovie.Loading to false,
                AsyncMovie.Failed(mockk()) to false,
                AsyncMovie.Empty to false,
                AsyncMovie.Single(mockk<Movie.ForSearch>()) to true,
                AsyncMovie.Multiple(emptyList<Movie.ForSearch>()) to true,
            )

        hasMoviePairs.forEach { (asyncMovie, expectedResult) ->
            assertEquals(
                expectedResult,
                asyncMovie.hasMovie(),
            )
        }
    }

    @Test
    fun `single movie to list`() {
        // Given
        val movie = TestAMovie.forSearch(title = "Romantic night")

        // When
        val movieList = AsyncMovie.Single(movie).toList<Movie.ForSearch>()

        // Then
        assertEquals(listOf(movie), movieList)
    }

    @Test
    fun `multiple movies to list`() {
        // Given
        val movies =
            listOf(
                TestAMovie.forSearch(title = "The wild bunch"),
                TestAMovie.forSearch(title = "Au Reservoir Les Oh Fun"),
            )

        // When
        val movieList = AsyncMovie.Multiple(movies).toList<Movie.ForSearch>()

        // Then
        assertEquals(movies, movieList)
    }

    @Test
    fun `empty list`() {
        val movieList = AsyncMovie.Empty.toList<Movie>()

        assertEquals(emptyList<Movie>(), movieList)
    }

    @Test
    fun `filter list - filter to empty`() {
        val filtered =
            AsyncMovie
                .Multiple(
                    listOf(
                        TestAMovie.forSearch(title = "The Thin Red Line"),
                        TestAMovie.forSearch(title = "Badlands"),
                        TestAMovie.forSearch(title = "Knight of Cups"),
                    ),
                ).filter { it.searchData?.title?.contains("wild") == true }

        assertEquals(AsyncMovie.Empty, filtered)
    }

    @Test
    fun `filter list - filter to one`() {
        val filtered =
            AsyncMovie
                .Multiple(
                    listOf(
                        TestAMovie.forSearch(title = "The Thin Red Line"),
                        TestAMovie.forSearch(title = "Badlands"),
                        TestAMovie.forSearch(title = "Knight of Cups"),
                    ),
                ).filter { it.searchData?.title?.contains("Red") == true }

        assertEquals(
            AsyncMovie.Single(TestAMovie.forSearch(title = "The Thin Red Line")),
            filtered,
        )
    }

    @Test
    fun `filter list - filter to multiple`() {
        val filtered =
            AsyncMovie
                .Multiple(
                    listOf(
                        TestAMovie.forSearch(title = "The Thin Red Line"),
                        TestAMovie.forSearch(title = "Badlands"),
                        TestAMovie.forSearch(title = "Knight of Cups"),
                    ),
                ).filter { it.searchData?.title?.contains("i") == true }
        assertEquals(
            AsyncMovie.Multiple(
                listOf(
                    TestAMovie.forSearch(title = "The Thin Red Line"),
                    TestAMovie.forSearch(title = "Knight of Cups"),
                ),
            ),
            filtered,
        )
    }

    @Test
    fun `filter list - empty stays empty`() {
        val filtered = AsyncMovie.Empty.filter { true }
        assertEquals(AsyncMovie.Empty, filtered)
    }

    @Test
    fun `filter list - single input stays`() {
        val filtered =
            AsyncMovie
                .Single(TestAMovie.forSearch(title = "Forrest Gump"))
                .filter { true }
        assertEquals(
            AsyncMovie
                .Single(TestAMovie.forSearch(title = "Forrest Gump")),
            filtered,
        )
    }

    @Test
    fun `filter list - single input goes`() {
        val filtered =
            AsyncMovie
                .Single(TestAMovie.forSearch(title = "Forrest Gump"))
                .filter { false }
        assertEquals(AsyncMovie.Empty, filtered)
    }
}
