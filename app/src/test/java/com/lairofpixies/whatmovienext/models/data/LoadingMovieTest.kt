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

class LoadingMovieTest {
    @Test
    fun `from list when list is empty`() {
        val loadingMovie = LoadingMovie.fromList(emptyList())
        assertEquals(
            LoadingMovie.Empty,
            loadingMovie,
        )
    }

    @Test
    fun `from list when list has one item`() {
        // Given
        val movie = Movie(title = "average movie")

        // When
        val loadingMovie = LoadingMovie.fromList(listOf(movie))

        // Then
        assertEquals(
            LoadingMovie.Single(movie),
            loadingMovie,
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
        val loadingMovie = LoadingMovie.fromList(movies)

        // Then
        assertEquals(
            LoadingMovie.Multiple(movies),
            loadingMovie,
        )
    }

    @Test
    fun `is missing table`() {
        val isMissingPairs: List<Pair<LoadingMovie?, Boolean>> =
            listOf(
                null to true,
                LoadingMovie.Loading to false,
                LoadingMovie.Failed(mockk()) to true,
                LoadingMovie.Empty to true,
                LoadingMovie.Single(mockk()) to false,
                LoadingMovie.Multiple(emptyList()) to false,
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
        val hasMoviePairs: List<Pair<LoadingMovie?, Boolean>> =
            listOf(
                null to false,
                LoadingMovie.Loading to false,
                LoadingMovie.Failed(mockk()) to false,
                LoadingMovie.Empty to false,
                LoadingMovie.Single(mockk()) to true,
                LoadingMovie.Multiple(emptyList()) to true,
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
        val movieList = LoadingMovie.Single(movie).toList()

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
        val movieList = LoadingMovie.Multiple(movies).toList()

        // Then
        assertEquals(movies, movieList)
    }

    @Test
    fun `empty list`() {
        val movieList = LoadingMovie.Empty.toList()

        assertEquals(emptyList<Movie>(), movieList)
    }

    @Test
    fun `filter list - filter to empty`() {
        val filtered =
            LoadingMovie
                .Multiple(
                    listOf(
                        Movie(title = "The Thin Red Line"),
                        Movie(title = "Badlands"),
                        Movie(title = "Knight of Cups"),
                    ),
                ).filter { it.title.contains("wild") }

        assertEquals(LoadingMovie.Empty, filtered)
    }

    @Test
    fun `filter list - filter to one`() {
        val filtered =
            LoadingMovie
                .Multiple(
                    listOf(
                        Movie(title = "The Thin Red Line"),
                        Movie(title = "Badlands"),
                        Movie(title = "Knight of Cups"),
                    ),
                ).filter { it.title.contains("Red") }

        assertEquals(LoadingMovie.Single(Movie(title = "The Thin Red Line")), filtered)
    }

    @Test
    fun `filter list - filter to multiple`() {
        val filtered =
            LoadingMovie
                .Multiple(
                    listOf(
                        Movie(title = "The Thin Red Line"),
                        Movie(title = "Badlands"),
                        Movie(title = "Knight of Cups"),
                    ),
                ).filter { it.title.contains("i") }
        assertEquals(
            LoadingMovie.Multiple(
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
        val filtered = LoadingMovie.Empty.filter { true }
        assertEquals(LoadingMovie.Empty, filtered)
    }

    @Test
    fun `filter list - single input stays`() {
        val filtered =
            LoadingMovie
                .Single(Movie(title = "Forrest Gump"))
                .filter { true }
        assertEquals(
            LoadingMovie
                .Single(Movie(title = "Forrest Gump")),
            filtered,
        )
    }

    @Test
    fun `filter list - single input goes`() {
        val filtered =
            LoadingMovie
                .Single(Movie(title = "Forrest Gump"))
                .filter { false }
        assertEquals(LoadingMovie.Empty, filtered)
    }
}
