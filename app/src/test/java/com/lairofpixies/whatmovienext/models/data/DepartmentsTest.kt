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

import org.junit.Assert.assertEquals
import org.junit.Test

class DepartmentsTest {
    data class Candidate(
        val dept: String?,
        val job: String?,
        val expectedMatch: Departments?,
    )

    private val candidates =
        listOf(
            Candidate(null, null, null),
            Candidate(null, "romulus", null),
            Candidate("acting", null, null),
            Candidate("acting", "romulus", Departments.Actors),
            Candidate("Acting", "Romulus", Departments.Actors),
            Candidate("directing", "director", Departments.Directors),
            Candidate("Directing", "Director", Departments.Directors),
            Candidate("directing", "assistant", null),
            Candidate("directing", null, null),
            Candidate("writing", "screenplay", Departments.Writers),
            Candidate("writing", "novel", Departments.Writers),
            Candidate("writing", "writer", Departments.Writers),
            Candidate("Writing", "Novel", Departments.Writers),
            Candidate("Writing", null, null),
            Candidate("Writing", "assistant", null),
        )

    @Test
    fun `match actors`() {
        // When
        val result = candidates.filter { Departments.Actors.matcher(it.dept, it.job) }
        // Then
        val expected = candidates.filter { it.expectedMatch == Departments.Actors }
        assertEquals(expected, result)
    }

    @Test
    fun `match writers`() {
        // When
        val result = candidates.filter { Departments.Writers.matcher(it.dept, it.job) }
        // Then
        val expected = candidates.filter { it.expectedMatch == Departments.Writers }
        assertEquals(expected, result)
    }

    @Test
    fun `match directors`() {
        // When
        val result = candidates.filter { Departments.Directors.matcher(it.dept, it.job) }
        // Then
        val expected = candidates.filter { it.expectedMatch == Departments.Directors }
        assertEquals(expected, result)
    }
}
