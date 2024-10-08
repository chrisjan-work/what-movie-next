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

@Entity
data class DbMovie(
    @PrimaryKey(autoGenerate = true) val movieId: Long = MovieData.NEW_ID,
    val title: String,
    val creationTime: Long = System.currentTimeMillis(),
    val tmdbId: Long = MovieData.UNKNOWN_ID,
    val imdbId: String? = null,
    val originalTitle: String = "",
    val year: Int? = null,
    val thumbnailUrl: String = "",
    val coverUrl: String = "",
    val tagline: String = "",
    val plot: String = "",
    val genreIds: String = "",
    val runtimeMinutes: Int = 0,
    val directorNames: String = "",
    val dbWatchDates: String = "",
    val isArchived: Boolean = false,
    val rtId: String = "",
    val rtRating: Int = -1,
    val mcId: String = "",
    val mcRating: Int = -1,
)

// a movie with people doing roles
data class DbStaffedMovie(
    @Embedded val movie: DbMovie,
    @Relation(
        entity = DbRole::class,
        parentColumn = "movieId",
        entityColumn = "movieId",
    )
    val staff: List<DbStaff>,
)
