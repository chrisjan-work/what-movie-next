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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteMovieSummary(
    @Json(name = "id")
    val tmdbId: Int,
    @Json(name = "title")
    val title: String,
    @Json(name = "original_title")
    val originalTitle: String = "",
    @Json(name = "release_date")
    val releaseDate: String = "",
    @Json(name = "poster_path")
    val posterPath: String? = null,
    @Json(name = "genre_ids")
    val genreIds: List<Int> = emptyList(),
)
