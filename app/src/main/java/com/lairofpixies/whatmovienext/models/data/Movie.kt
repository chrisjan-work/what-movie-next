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

data class Movie(
    val id: Long = NEW_ID,
    val title: String,
    val tmdbId: Long = UNKNOWN_ID,
    val imdbId: String? = null,
    val originalTitle: String = "",
    val year: Int? = null,
    val thumbnailUrl: String = "",
    val coverUrl: String = "",
    val tagline: String = "",
    val summary: String = "",
    val genres: List<String> = emptyList(),
    val runtimeMinutes: Int = 0,
    val watchState: WatchState = WatchState.PENDING,
    val isArchived: Boolean = false,
) {
    companion object {
        const val NEW_ID = 0L
        const val UNKNOWN_ID = -1L
    }

    fun hasSaveableChangesSince(lastSavedMovie: Movie?): Boolean =
        when {
            title.isBlank() -> false
            lastSavedMovie == null -> true
            else -> title != lastSavedMovie.title
        }

    fun hasQuietSaveableChangesSince(lastSavedMovie: Movie?): Boolean =
        when {
            title.isBlank() -> false
            lastSavedMovie == null -> true
            else -> watchState != lastSavedMovie.watchState
        }

    fun isNew(): Boolean = id == Movie.NEW_ID

    fun printableRuntime(
        pre: String = "",
        pos: String = "",
    ): String =
        when (runtimeMinutes) {
            0 -> ""
            in 1..59 -> "$pre$runtimeMinutes min$pos"
            else -> "$pre${runtimeMinutes / 60}h ${runtimeMinutes % 60}min$pos"
        }
}
