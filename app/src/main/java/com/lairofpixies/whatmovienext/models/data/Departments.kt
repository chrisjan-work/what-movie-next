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

enum class Departments(
    val department: String,
    val matcher: (dept: String?, job: String?) -> Boolean,
) {
    Actors("acting", { dept, job -> dept?.lowercase() == Actors.department && job != null }),
    Writers(
        "writing",
        { dept, job ->
            dept?.lowercase() == Writers.department && job?.lowercase() in WRITING_JOBS
        },
    ),
    Directors("directing", { dept, job ->
        dept?.lowercase() == Directors.department && job?.lowercase() == DIRECTOR_JOBS
    }),
}

private val WRITING_JOBS = setOf("screenplay", "novel", "writer")
private const val DIRECTOR_JOBS = "director"
