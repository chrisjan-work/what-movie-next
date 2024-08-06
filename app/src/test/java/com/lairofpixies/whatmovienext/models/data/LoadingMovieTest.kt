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
        val loadingMovie = LoadingAMovie.fromList(emptyList())
        assertEquals(
            LoadingAMovie.Empty,
            loadingMovie,
        )
    }

    @Test
    fun `from list when list has one item`() {
        // Given
        val movie = TestAMovie.forSearch(title = "average movie")

        // When
        val loadingMovie = LoadingAMovie.fromList(listOf(movie))

        // Then
        assertEquals(
            LoadingAMovie.Single(movie),
            loadingMovie,
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
        val loadingMovie = LoadingAMovie.fromList(movies)

        // Then
        assertEquals(
            LoadingAMovie.Multiple(movies),
            loadingMovie,
        )
    }

    @Test
    fun `is missing table`() {
        val isMissingPairs: List<Pair<LoadingAMovie?, Boolean>> =
            listOf(
                null to true,
                LoadingAMovie.Loading to false,
                LoadingAMovie.Failed(mockk()) to true,
                LoadingAMovie.Empty to true,
                LoadingAMovie.Single(mockk<Movie.ForSearch>()) to false,
                LoadingAMovie.Multiple(emptyList<Movie.ForSearch>()) to false,
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
        val hasMoviePairs: List<Pair<LoadingAMovie?, Boolean>> =
            listOf(
                null to false,
                LoadingAMovie.Loading to false,
                LoadingAMovie.Failed(mockk()) to false,
                LoadingAMovie.Empty to false,
                LoadingAMovie.Single(mockk<Movie.ForSearch>()) to true,
                LoadingAMovie.Multiple(emptyList<Movie.ForSearch>()) to true,
            )

        hasMoviePairs.forEach { (loadingMovie, expectedResult) ->
            assertEquals(
                expectedResult,
                loadingMovie.hasMovie(),
            )
        }
    }

    @Test
    fun `single movie to list`() {
        // Given
        val movie = TestAMovie.forSearch(title = "Romantic night")

        // When
        val movieList = LoadingAMovie.Single(movie).toList<Movie.ForSearch>()

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
        val movieList = LoadingAMovie.Multiple(movies).toList<Movie.ForSearch>()

        // Then
        assertEquals(movies, movieList)
    }

    @Test
    fun `empty list`() {
        val movieList = LoadingAMovie.Empty.toList<Movie>()

        assertEquals(emptyList<Movie>(), movieList)
    }

    @Test
    fun `filter list - filter to empty`() {
        val filtered =
            LoadingAMovie
                .Multiple(
                    listOf(
                        TestAMovie.forSearch(title = "The Thin Red Line"),
                        TestAMovie.forSearch(title = "Badlands"),
                        TestAMovie.forSearch(title = "Knight of Cups"),
                    ),
                ).filter { it.searchData?.title?.contains("wild") == true }

        assertEquals(LoadingAMovie.Empty, filtered)
    }

    @Test
    fun `filter list - filter to one`() {
        val filtered =
            LoadingAMovie
                .Multiple(
                    listOf(
                        TestAMovie.forSearch(title = "The Thin Red Line"),
                        TestAMovie.forSearch(title = "Badlands"),
                        TestAMovie.forSearch(title = "Knight of Cups"),
                    ),
                ).filter { it.searchData?.title?.contains("Red") == true }

        assertEquals(
            LoadingAMovie.Single(TestAMovie.forSearch(title = "The Thin Red Line")),
            filtered,
        )
    }

    @Test
    fun `filter list - filter to multiple`() {
        val filtered =
            LoadingAMovie
                .Multiple(
                    listOf(
                        TestAMovie.forSearch(title = "The Thin Red Line"),
                        TestAMovie.forSearch(title = "Badlands"),
                        TestAMovie.forSearch(title = "Knight of Cups"),
                    ),
                ).filter { it.searchData?.title?.contains("i") == true }
        assertEquals(
            LoadingAMovie.Multiple(
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
        val filtered = LoadingAMovie.Empty.filter { true }
        assertEquals(LoadingAMovie.Empty, filtered)
    }

    @Test
    fun `filter list - single input stays`() {
        val filtered =
            LoadingAMovie
                .Single(TestAMovie.forSearch(title = "Forrest Gump"))
                .filter { true }
        assertEquals(
            LoadingAMovie
                .Single(TestAMovie.forSearch(title = "Forrest Gump")),
            filtered,
        )
    }

    @Test
    fun `filter list - single input goes`() {
        val filtered =
            LoadingAMovie
                .Single(TestAMovie.forSearch(title = "Forrest Gump"))
                .filter { false }
        assertEquals(LoadingAMovie.Empty, filtered)
    }
}
