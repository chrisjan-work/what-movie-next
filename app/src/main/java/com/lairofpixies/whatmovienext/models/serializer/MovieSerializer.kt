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
package com.lairofpixies.whatmovienext.models.serializer

import com.lairofpixies.whatmovienext.models.data.MovieDump
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

class MovieSerializer(
    private val movieRepository: MovieRepository,
    private val adapter: JsonAdapter<MovieDump>,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val serializerScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    suspend fun fullMoviesJson(): String =
        serializerScope
            .async {
                val movieDump = movieRepository.retrieveFullMovieDump()
                adapter.toJson(movieDump)
            }.await()
}
