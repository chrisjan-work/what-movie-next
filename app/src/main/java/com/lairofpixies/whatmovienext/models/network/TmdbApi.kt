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
package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.network.data.TmdbConfiguration
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import com.lairofpixies.whatmovienext.models.network.data.TmdbSearchResults
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("search/movie")
    suspend fun findMoviesByTitle(
        @Query("query") escapedTitle: String,
        @Query("page") page: Int? = null,
        @Query("language") language: String,
    ): TmdbSearchResults

    @GET("configuration")
    suspend fun getConfiguration(): TmdbConfiguration

    @GET("movie/{tmdbId}?append_to_response=credits")
    suspend fun getMovieDetails(
        @Path("tmdbId") tmdbId: Long,
        @Query("language") language: String,
    ): TmdbMovieExtended
}
