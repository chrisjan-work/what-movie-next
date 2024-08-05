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

import com.lairofpixies.whatmovienext.models.data.LoadingMovie
import com.lairofpixies.whatmovienext.models.mappers.RemoteMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import retrofit2.HttpException

class ApiRepositoryImpl(
    private val tmdbApi: TmdbApi,
    private val remoteMapper: RemoteMapper,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ApiRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun findMoviesByTitle(title: String): StateFlow<LoadingMovie> =
        flow {
            try {
                val remoteMovies =
                    repositoryScope
                        .async {
                            tmdbApi.findMoviesByTitle(escapeForQuery(title))
                        }.await()

                val loadingMovie =
                    LoadingMovie.fromList(
                        remoteMovies.results.map { remoteMovie ->
                            remoteMapper.toMovie(remoteMovie)
                        },
                    )
                emit(loadingMovie)
            } catch (httpException: HttpException) {
                emit(LoadingMovie.Failed(httpException))
            } catch (exception: Exception) {
                emit(LoadingMovie.Failed(exception))
            }
        }.stateIn(
            repositoryScope,
            SharingStarted.Eagerly,
            initialValue = LoadingMovie.Loading,
        )

    private fun escapeForQuery(title: String) = title.trim().replace(" ", "+")

    override fun getMovieDetails(tmdbId: Long): StateFlow<LoadingMovie> =
        flow {
            try {
                val remoteMovie = tmdbApi.getMovieDetails(tmdbId)
                if (remoteMovie.success == false) {
                    emit(LoadingMovie.Failed(Exception("Failed to get movie details")))
                    return@flow
                }
                val movie = remoteMapper.toMovie(remoteMovie)
                emit(LoadingMovie.Single(movie))
            } catch (httpException: HttpException) {
                emit(LoadingMovie.Failed(httpException))
            } catch (exception: Exception) {
                emit(LoadingMovie.Failed(exception))
            }
        }.stateIn(
            repositoryScope,
            SharingStarted.Eagerly,
            initialValue = LoadingMovie.Loading,
        )
}
