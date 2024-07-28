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

class BackendConfigRepositoryImpl(
    // TODO: fetch from movieApi
//    private val movieApi: MovieApi,
//    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BackendConfigRepository {
    private var baseUrl: String = ""
    private var smallOption: String = ""
    private var bigOption: String = ""

    init {
        baseUrl = "https://image.tmdb.org/t/p/"
        smallOption = "w154"
        bigOption = "w500"
    }

    override fun getThumbnailUrl(posterPath: String?): String = if (!posterPath.isNullOrBlank()) "$baseUrl$smallOption$posterPath" else ""

    override fun getCoverUrl(posterPath: String?): String = if (!posterPath.isNullOrBlank()) "$baseUrl$bigOption$posterPath" else ""
}
