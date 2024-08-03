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
package com.lairofpixies.whatmovienext.models.database

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.mappers.DbMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MovieRepositoryImpl(
    private val dao: MovieDao,
    private val dbMapper: DbMapper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MovieRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override val movies: Flow<AsyncMovieInfo> =
        dao
            .getAllMovies()
            .map { dbMovies ->
                dbMapper.toAsyncMovies(dbMovies)
            }.flowOn(ioDispatcher)

    override val archivedMovies: Flow<AsyncMovieInfo> =
        dao
            .getArchivedMovies()
            .map { dbMovies ->
                dbMapper.toAsyncMovies(dbMovies)
            }.flowOn(ioDispatcher)

    override fun singleMovie(movieId: Long): Flow<AsyncMovieInfo> =
        dao
            .getMovie(movieId)
            .map { maybeMovie ->
                maybeMovie
                    ?.let { dbMapper.toMovie(it) }
                    ?.let { AsyncMovieInfo.Single(it) }
                    ?: AsyncMovieInfo.Empty
            }.flowOn(ioDispatcher)

    override suspend fun fetchMovieById(movieId: Long): Movie? =
        repositoryScope
            .async {
                if (movieId != Movie.NEW_ID) {
                    dao.fetchMovieById(movieId)?.let {
                        dbMapper.toMovie(it)
                    }
                } else {
                    null
                }
            }.await()

    override suspend fun fetchMoviesByTitle(movieTitle: String): List<Movie> =
        repositoryScope
            .async {
                dbMapper.toMovies(dao.fetchMoviesByTitle(movieTitle))
            }.await()

    override suspend fun addMovie(movie: Movie): Long =
        repositoryScope
            .async {
                dao.insertMovie(dbMapper.toDbMovie(movie))
            }.await()

    override suspend fun updateMovie(movie: Movie): Long {
        repositoryScope
            .launch {
                dao.updateMovie(dbMapper.toDbMovie(movie))
            }.join()
        return movie.id
    }

    override suspend fun setWatchState(
        movieId: Long,
        watchState: WatchState,
    ) {
        repositoryScope
            .launch {
                dao.updateWatchState(movieId, watchState)
            }
    }

    override suspend fun archiveMovie(movieId: Long) {
        repositoryScope
            .launch {
                dao.archive(movieId)
            }
    }

    override suspend fun restoreMovie(movieId: Long) {
        repositoryScope
            .launch {
                dao.restore(movieId)
            }
    }

    override suspend fun deleteMovie(movieId: Long) {
        repositoryScope
            .launch {
                val dbMovie = dao.fetchMovieById(movieId)
                dbMovie?.let { dao.delete(dbMovie) }
            }
    }
}
