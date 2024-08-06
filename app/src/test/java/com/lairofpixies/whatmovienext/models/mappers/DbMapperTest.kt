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

import com.lairofpixies.whatmovienext.models.data.MovieData
import com.lairofpixies.whatmovienext.models.data.Staff
import com.lairofpixies.whatmovienext.models.database.data.DbPerson
import com.lairofpixies.whatmovienext.models.database.data.DbRole
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DbMapperTest {
    private lateinit var dbMapper: DbMapper

    @Before
    fun setUp() {
        dbMapper = DbMapper()
    }

    @Test
    fun `db movie to card movie`() {
        // Given
        val dbMovie = testDbMovieExtended()

        // When
        val result = dbMapper.toCardMovie(dbMovie)

        // Then
        val expected = testCardMovieExtended().copy(staffData = MovieData.StaffData())

        assertEquals(expected, result)
    }

    @Test
    fun `card movie to db movie`() {
        // Given
        val cardMovie = testCardMovieExtended()

        // When
        val result = dbMapper.toDbMovie(cardMovie)

        // Then
        val expected = testDbMovieExtended()
        assertEquals(expected, result)
    }

    @Test
    fun `db movie to list movie`() {
        // Given
        val dbMovie = testDbMovieExtended()

        // When
        val result = dbMapper.toListMovie(dbMovie)

        // Then
        val expected = testListMovieExtended()
        assertEquals(expected, result)
    }

    @Test
    fun toGenres() {
        // When
        val result = dbMapper.toGenres("Character Study,Musical")
        // Then
        assertEquals(listOf("Character Study", "Musical"), result)
    }

    @Test
    fun toDbGenres() {
        // When
        val result = dbMapper.toDbGenres(listOf("Character Study", "Musical"))
        // Then
        assertEquals("Character Study,Musical", result)
    }

    @Test
    fun toDbPeople() {
        // Given
        val people =
            listOf(
                Staff(
                    personId = 1,
                    roleId = 11,
                    name = "Mandy",
                    originalName = "Amanda",
                    faceUrl = "/manda.jpg",
                    credit = "her",
                    dept = "acting",
                    order = 1,
                ),
            )

        // When
        val result = dbMapper.toDbPeople(people)

        // Then
        val expected =
            listOf(
                DbPerson(
                    personId = 1,
                    name = "Mandy",
                    originalName = "Amanda",
                    faceUrl = "/manda.jpg",
                ),
            )
        assertEquals(expected, result)
    }

    @Test
    fun toDbRoles() {
        // Given
        val people =
            listOf(
                Staff(
                    personId = 1,
                    roleId = 11,
                    name = "Mandy",
                    originalName = "Amanda",
                    faceUrl = "/manda.jpg",
                    credit = "her",
                    dept = "acting",
                    order = 10,
                ),
            )

        // When
        val result = dbMapper.toDbRoles(99, people)

        // Then
        val expected =
            listOf(
                DbRole(
                    personId = 1,
                    movieId = 99,
                    credit = "her",
                    dept = "acting",
                    order = 10,
                ),
            )
        assertEquals(expected, result)
    }
}
