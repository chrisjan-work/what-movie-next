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

object MovieData {
    data class AppData(
        val movieId: Long = NEW_ID,
        val creationTime: Long = System.currentTimeMillis(),
        val watchState: WatchState = WatchState.PENDING,
        val isArchived: Boolean = false,
    )

    data class SearchData(
        val title: String,
        val tmdbId: Long = UNKNOWN_ID,
        val originalTitle: String = "",
        val year: Int? = null,
        val thumbnailUrl: String = "",
        val coverUrl: String = "",
        val genres: List<String> = emptyList(),
    )

    data class DetailData(
        val imdbId: String? = null,
        val tagline: String = "",
        val plot: String = "",
        val runtimeMinutes: Int = 0,
    )

    data class StaffData(
        val cast: List<Staff> = emptyList(),
        val crew: List<Staff> = emptyList(),
    )

    const val NEW_ID = 0L
    const val UNKNOWN_ID = -1L
}
