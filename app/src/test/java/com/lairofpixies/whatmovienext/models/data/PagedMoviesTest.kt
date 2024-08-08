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

import com.lairofpixies.whatmovienext.models.data.TestMovie.forSearch
import org.junit.Assert.assertEquals
import org.junit.Test

class PagedMoviesTest {
    @Test
    fun `add to nothing returs itself`() {
        // Given
        val self =
            listOf(
                PagedMovies.Loading,
                PagedMovies.Empty,
                PagedMovies.Failed(Exception("error")),
                PagedMovies(AsyncMovie.Single(forSearch(title = "average movie"))),
                PagedMovies(
                    AsyncMovie.Multiple(
                        listOf(
                            forSearch(title = "first movie"),
                            forSearch(title = "second movie"),
                        ),
                    ),
                ),
            )

        // When
        val added = self.map { it.addTo(emptyList()) }

        // Then
        self.zip(added).forEach { (expected, result) ->
            assertEquals(expected, result)
        }
    }

    @Test
    fun `add to one updates the list`() {
        // Given
        val headMovie = forSearch(title = "head")
        val tailMovies =
            listOf(
                forSearch(title = "coming as single"),
                forSearch(title = "first of tail"),
                forSearch(title = "second op tail"),
            )
        val exception = Exception("error")
        val self =
            listOf(
                PagedMovies.Loading,
                PagedMovies.Failed(exception),
                PagedMovies.Empty,
                PagedMovies(AsyncMovie.Single(tailMovies.first())),
                PagedMovies(AsyncMovie.Multiple(tailMovies.takeLast(2))),
            )

        // When
        val resultList = self.map { it.addTo(listOf(headMovie)) }

        // Then
        val expectedList =
            listOf(
                PagedMovies.Loading,
                PagedMovies.Failed(exception),
                PagedMovies(AsyncMovie.Single(headMovie)),
                PagedMovies(AsyncMovie.Multiple(listOf(headMovie, tailMovies.first()))),
                PagedMovies(
                    AsyncMovie.Multiple(listOf(headMovie, tailMovies[1], tailMovies[2])),
                ),
            )

        expectedList.zip(resultList).forEach { (expected, result) ->
            assertEquals(expected, result)
        }
    }

    @Test
    fun `convenience fromList`() {
        // Given
        val twoMovies =
            listOf(
                forSearch(title = "top"),
                forSearch(title = "bottom"),
            )
        val inputs =
            listOf(
                emptyList(),
                twoMovies.take(1),
                twoMovies,
            )

        // When
        val resultList = inputs.map { PagedMovies.fromList(it) }

        // Then
        val expectedList =
            listOf(
                PagedMovies(AsyncMovie.Empty),
                PagedMovies(AsyncMovie.Single(twoMovies.first())),
                PagedMovies(AsyncMovie.Multiple(twoMovies)),
            )

        expectedList.zip(resultList).forEach { (expected, result) ->
            assertEquals(expected, result)
        }
    }
}
