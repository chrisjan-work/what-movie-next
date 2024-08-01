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
package com.lairofpixies.whatmovienext.models.network.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbMovieExtended(
    @Json(name = "success")
    val success: Boolean? = null,
    @Json(name = "id")
    val tmdbId: Long? = null,
    @Json(name = "imdb_id")
    val imdbId: String? = null,
    @Json(name = "title")
    val title: String? = null,
    @Json(name = "original_title")
    val originalTitle: String? = null,
    @Json(name = "poster_path")
    val posterPath: String? = null,
    @Json(name = "release_date")
    val releaseDate: String? = null,
    @Json(name = "tagline")
    val tagline: String? = null,
    @Json(name = "overview")
    val summary: String? = null,
    @Json(name = "runtime")
    val runtime: Int? = null,
    @Json(name = "genres")
    val genres: List<TmdbGenres.TmdbGenre>? = null,
    @Json(name = "credits")
    val credits: TmdbCredits? = null,
) {
    @JsonClass(generateAdapter = true)
    data class TmdbCredits(
        @Json(name = "cast")
        val cast: List<TmdbCastMember>? = null,
        @Json(name = "crew")
        val crew: List<TmdbCrewMember>? = null,
    )

    @JsonClass(generateAdapter = true)
    data class TmdbCastMember(
        @Json(name = "id")
        val tmdbId: Long,
        @Json(name = "name")
        val name: String,
        @Json(name = "original_name")
        val originalName: String?,
        @Json(name = "profile_path")
        val profilePath: String?,
        @Json(name = "character")
        val character: String?,
        @Json(name = "order")
        val order: Int = 0,
    )

    @JsonClass(generateAdapter = true)
    data class TmdbCrewMember(
        @Json(name = "id")
        val tmdbId: Long,
        @Json(name = "name")
        val name: String,
        @Json(name = "original_name")
        val originalName: String?,
        @Json(name = "profile_path")
        val profilePath: String?,
        @Json(name = "department")
        val department: String?,
        @Json(name = "job")
        val job: String?,
    )
}
