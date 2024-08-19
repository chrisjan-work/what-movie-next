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
package com.lairofpixies.whatmovienext.viewmodels.processors

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FilterProcessorTest {
    private lateinit var filterProcessor: FilterProcessor

    @Before
    fun setUp() {
        filterProcessor = FilterProcessor()
    }

    @Test
    fun `update movie list with all movies filter`() {
        // Given
        val seenMovie =
            forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
        val unseenMovie =
            forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())

        val movieList = AsyncMovie.Multiple(listOf(seenMovie, unseenMovie))
        val listFilters =
            ListFilters(
                listMode = ListMode.ALL,
            )

        // When
        val result = filterProcessor.filterMovies(movieList, listFilters)

        // Then
        assertEquals(movieList, result)
    }

    @Test
    fun `update movie list with only unseen movies`() {
        // Given
        val seenMovie =
            forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
        val unseenMovie =
            forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
        val movieList = AsyncMovie.Multiple(listOf(seenMovie, unseenMovie))
        val listFilters =
            ListFilters(
                listMode = ListMode.PENDING,
            )

        // When
        // When
        val result = filterProcessor.filterMovies(movieList, listFilters)

        // Then
        assertEquals(AsyncMovie.Single(unseenMovie), result)
    }

    @Test
    fun `update movie list with only seen movies`() {
        // Given
        val seenMovie =
            forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
        val unseenMovie =
            forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
        val movieList = AsyncMovie.Multiple(listOf(seenMovie, unseenMovie))
        val listFilters =
            ListFilters(
                listMode = ListMode.WATCHED,
            )

        // When
        val result = filterProcessor.filterMovies(movieList, listFilters)

        // Then
        assertEquals(AsyncMovie.Single(seenMovie), result)
    }
}
