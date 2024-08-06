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

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.mappers.RemoteMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ApiRepositoryImpl(
    private val tmdbApi: TmdbApi,
    private val remoteMapper: RemoteMapper,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ApiRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun findMoviesByTitle(title: String): Flow<AsyncMovie> =
        flow {
            emit(AsyncMovie.Loading)
            val remoteMovies =
                repositoryScope
                    .async {
                        tmdbApi.findMoviesByTitle(escapeForQuery(title))
                    }.await()

            val asyncMovie =
                AsyncMovie.fromList(
                    remoteMovies.results.map { remoteMovie ->
                        remoteMapper.toSearchMovie(remoteMovie)
                    },
                )
            emit(asyncMovie)
        }.catch { exception: Throwable ->
            emit(AsyncMovie.Failed(exception))
        }

    private fun escapeForQuery(title: String) = title.trim().replace(" ", "+")

    override fun getMovieDetails(tmdbId: Long): Flow<AsyncMovie> =
        flow {
            emit(AsyncMovie.Loading)

            val remoteMovie = tmdbApi.getMovieDetails(tmdbId)
            if (remoteMovie.success == false) {
                emit(AsyncMovie.Failed(Exception("Failed to get movie details")))
                return@flow
            }
            val movie = remoteMapper.toCardMovie(remoteMovie)
            emit(AsyncMovie.Single(movie))
        }.catch { exception: Throwable ->
            emit(AsyncMovie.Failed(exception))
        }
}
