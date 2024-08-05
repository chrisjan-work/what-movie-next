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

import com.lairofpixies.whatmovienext.models.data.AMovie
import com.lairofpixies.whatmovienext.models.data.LoadingAMovie
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

    override val listedMovies: Flow<LoadingAMovie> =
        dao
            .getAllMovies()
            .map { dbMovies ->
                LoadingAMovie.fromList(
                    dbMovies.map { dbMovie ->
                        dbMapper.toListMovie(dbMovie)
                    },
                )
            }.flowOn(ioDispatcher)

    override val archivedMovies: Flow<LoadingAMovie> =
        dao
            .getArchivedMovies()
            .map { dbMovies ->
                LoadingAMovie.fromList(
                    dbMovies.map { dbMovie ->
                        dbMapper.toListMovie(dbMovie)
                    },
                )
            }.flowOn(ioDispatcher)

    override fun singleCardMovie(movieId: Long): Flow<LoadingAMovie> =
        dao
            .getMovie(movieId)
            .map { maybeMovie ->
                maybeMovie
                    ?.let { dbMapper.toCardMovie(it) }
                    ?.let { LoadingAMovie.Single(it) }
                    ?: LoadingAMovie.Empty
            }.flowOn(ioDispatcher)

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

    override suspend fun storeMovie(movie: AMovie.ForCard) =
        repositoryScope.launch {
            val oldMovie = dao.fetchMovieByTmdbId(movie.searchData.tmdbId)
            if (oldMovie != null) {
                // if movie was already saved
                // update it while keeping the old app data
                // but unarchive it if it was archived
                val mappedOldMovie: AMovie.ForCard = dbMapper.toCardMovie(oldMovie)
                val movieToSave =
                    dbMapper.toDbMovie(
                        movie.copy(
                            appData =
                                mappedOldMovie.appData.copy(isArchived = false),
                        ),
                    )
                dao.updateMovie(movieToSave)
            } else {
                dao.insertMovie(dbMapper.toDbMovie(movie))
            }
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
                dbMovie?.let { dao.deleteMovie(dbMovie) }
            }
    }
}
