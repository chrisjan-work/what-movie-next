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
package com.lairofpixies.whatmovienext.models.mappers

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GenreMapperTest {
    private lateinit var mockContext: Context
    private lateinit var genreMapper: GenreMapper

    @Before
    fun setUp() {
        mockContext = testGenreMapperContext()
        genreMapper = GenreMapper(mockContext)
    }

    @Test
    fun `ids to genre names`() {
        // Given
        val ids: List<Long> = listOf(14, 27, 36)
        // When
        val names = genreMapper.toGenreNames(ids)

        // Then
        val expected = listOf("Fantasy", "Horror", "History")

        assertEquals(expected, names)
    }

    @Test
    fun `pack genre ids for database`() {
        // Given
        val ids: List<Long> = listOf(14, 27, 36)

        // When
        val dbGenreIds = genreMapper.toDbGenreIds(ids)

        // Then
        val expected = "14,27,36"

        assertEquals(expected, dbGenreIds)
    }

    @Test
    fun `unpack genre ids from database`() {
        // Given
        val dbGenreIds = "14,27,36"

        // When
        val ids = genreMapper.toGenreIds(dbGenreIds)

        // Then
        val expected: List<Long> = listOf(14, 27, 36)

        assertEquals(expected, ids)
    }

    @Test
    fun allGenreNamesMap() {
        val theMap = genreMapper.allGenreNamesMap()

        // pointless to duplicate the map here and check one by one
        // we'll just go with size as a rough check
        assertEquals(19, theMap.size)
    }
}
