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

    @Test
    fun `filter list - filter to empty`() {
        val filtered =
            AsyncMovieInfo
                .Multiple(
                    listOf(
                        Movie(title = "The Thin Red Line"),
                        Movie(title = "Badlands"),
                        Movie(title = "Knight of Cups"),
                    ),
                ).filter { it.title.contains("wild") }

        assertEquals(AsyncMovieInfo.Empty, filtered)
    }

    @Test
    fun `filter list - filter to one`() {
        val filtered =
            AsyncMovieInfo
                .Multiple(
                    listOf(
                        Movie(title = "The Thin Red Line"),
                        Movie(title = "Badlands"),
                        Movie(title = "Knight of Cups"),
                    ),
                ).filter { it.title.contains("Red") }

        assertEquals(AsyncMovieInfo.Single(Movie(title = "The Thin Red Line")), filtered)
    }

    @Test
    fun `filter list - filter to multiple`() {
        val filtered =
            AsyncMovieInfo
                .Multiple(
                    listOf(
                        Movie(title = "The Thin Red Line"),
                        Movie(title = "Badlands"),
                        Movie(title = "Knight of Cups"),
                    ),
                ).filter { it.title.contains("i") }
        assertEquals(
            AsyncMovieInfo.Multiple(
                listOf(
                    Movie(title = "The Thin Red Line"),
                    Movie(title = "Knight of Cups"),
                ),
            ),
            filtered,
        )
    }

    @Test
    fun `filter list - empty stays empty`() {
        val filtered = AsyncMovieInfo.Empty.filter { true }
        assertEquals(AsyncMovieInfo.Empty, filtered)
    }

    @Test
    fun `filter list - single input stays`() {
        val filtered =
            AsyncMovieInfo
                .Single(Movie(title = "Forrest Gump"))
                .filter { true }
        assertEquals(
            AsyncMovieInfo
                .Single(Movie(title = "Forrest Gump")),
            filtered,
        )
    }

    @Test
    fun `filter list - single input goes`() {
        val filtered =
            AsyncMovieInfo
                .Single(Movie(title = "Forrest Gump"))
                .filter { false }
        assertEquals(AsyncMovieInfo.Empty, filtered)
    }
}
