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
package com.lairofpixies.whatmovienext.models.database.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.lairofpixies.whatmovienext.models.data.MovieData

// a person is a director, actor or writer
@Entity
data class DbPerson(
    @PrimaryKey(autoGenerate = false)
    val personId: Long = MovieData.UNKNOWN_ID,
    val name: String = "",
    val originalName: String = "",
    val faceUrl: String = "",
)

// staff are people with roles
data class DbStaff(
    @Embedded val role: DbRole,
    @Relation(
        parentColumn = "personId",
        entityColumn = "personId",
    )
    val person: DbPerson,
)
