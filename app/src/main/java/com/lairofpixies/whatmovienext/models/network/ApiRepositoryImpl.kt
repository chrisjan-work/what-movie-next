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
import com.lairofpixies.whatmovienext.models.data.PagedMovies
import com.lairofpixies.whatmovienext.models.data.RatingPair
import com.lairofpixies.whatmovienext.models.mappers.RemoteMapper
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo
import com.lairofpixies.whatmovienext.util.LanguageProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class ApiRepositoryImpl(
    private val tmdbApi: TmdbApi,
    private val omdbApi: OmdbApi,
    private val wikidataApi: WikidataApi,
    private val remoteMapper: RemoteMapper,
    private val languageProvider: LanguageProvider,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ApiRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun findMoviesByTitle(
        title: String,
        page: Int?,
    ): Flow<PagedMovies> =
        flow {
            emit(PagedMovies.Loading)
            val remoteMovies =
                repositoryScope
                    .async {
                        tmdbApi.findMoviesByTitle(escapeForQuery(title), page, language = languageProvider.current)
                    }.await()

            val asyncMovie =
                AsyncMovie.fromList(
                    remoteMovies.results.map { remoteMovie ->
                        remoteMapper.toSearchMovie(remoteMovie)
                    },
                )
            val pagedMovies =
                PagedMovies(
                    movies = asyncMovie,
                    lastPage = remoteMovies.page,
                    pagesLeft = remoteMovies.totalPages - remoteMovies.page,
                )
            emit(pagedMovies)
        }.catch { exception: Throwable ->
            emit(PagedMovies.Failed(exception))
        }

    private fun escapeForQuery(title: String) = title.trim().replace(" ", "+")

    override fun getMovieDetails(tmdbId: Long): Flow<AsyncMovie> =
        flow {
            emit(AsyncMovie.Loading)

            val remoteMovie = tmdbApi.getMovieDetails(tmdbId, language = languageProvider.current)
            if (remoteMovie.success == false) {
                emit(AsyncMovie.Failed(Exception("Failed to get movie details")))
                return@flow
            }

            val ratings =
                if (!remoteMovie.imdbId.isNullOrBlank()) {
                    val omdbRatings =
                        try {
                            omdbApi.fetchMovieRatings(remoteMovie.imdbId)
                        } catch (e: Throwable) {
                            Timber.e("omdbApi call failed with error $e")
                            null
                        }
                    val wikidataRatings =
                        try {
                            wikidataApi.askSparql(WikidataMovieInfo.sparqlQuery(remoteMovie.imdbId))
                        } catch (e: Throwable) {
                            Timber.e("wikidataApi call failed with error $e")
                            null
                        }

                    remoteMapper.toRatings(omdbRatings, wikidataRatings)
                } else {
                    RatingPair()
                }
            val movie = remoteMapper.toCardMovie(remoteMovie, ratings)
            emit(AsyncMovie.Single(movie))
        }.catch { exception: Throwable ->
            emit(AsyncMovie.Failed(exception))
        }
}
