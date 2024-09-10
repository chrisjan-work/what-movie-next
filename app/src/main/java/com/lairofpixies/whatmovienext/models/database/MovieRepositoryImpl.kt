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

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Departments
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.MovieData
import com.lairofpixies.whatmovienext.models.database.data.DbPerson
import com.lairofpixies.whatmovienext.models.database.data.DbRole
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

    override val listedMovies: Flow<AsyncMovie> =
        dao
            .getAllMovies()
            .map { dbMovies ->
                AsyncMovie.fromList(
                    dbMovies.map { dbMovie ->
                        dbMapper.toListMovie(dbMovie)
                    },
                )
            }.flowOn(ioDispatcher)

    override val archivedMovies: Flow<AsyncMovie> =
        dao
            .getArchivedMovies()
            .map { dbMovies ->
                AsyncMovie.fromList(
                    dbMovies.map { dbMovie ->
                        dbMapper.toListMovie(dbMovie)
                    },
                )
            }.flowOn(ioDispatcher)

    override val isEmpty: Flow<Boolean> =
        dao
            .getOneMovie()
            .map { it == null }
            .flowOn(ioDispatcher)

    override fun singleCardMovie(movieId: Long): Flow<AsyncMovie> =
        dao
            .getStaffedMovie(movieId)
            .map { maybeMovie ->
                maybeMovie
                    ?.let { dbMapper.toCardMovie(it) }
                    ?.let { AsyncMovie.Single(it) }
                    ?: AsyncMovie.Empty
            }.flowOn(ioDispatcher)

    override suspend fun retrieveFullMovieDump(): List<Movie.ForCard> =
        repositoryScope
            .async {
                dao
                    .allStaffedMovies()
                    .map { dbMovie ->
                        dbMapper.toCardMovie(dbMovie)
                    }
            }.await()

    override fun getAllPeopleNamesByDepartment(department: Departments): Flow<List<String>> =
        dao
            .getStaffByDepartment(department.department)
            .map { staffList ->
                staffList
                    .map { staff ->
                        staff.person.name
                    }.distinct()
            }.flowOn(ioDispatcher)

    override fun getAllGenresFromMovies(): Flow<List<String>> =
        dao
            .getAllMovies()
            .map { dbMovies ->
                dbMovies
                    .flatMap { dbMovie ->
                        dbMapper.toGenres(dbMovie.genres)
                    }.distinct()
            }.flowOn(ioDispatcher)

    override suspend fun fetchMovieIdFromTmdbId(tmdbId: Long): Long? =
        repositoryScope
            .async {
                dao.fetchMovieByTmdbId(tmdbId)?.movie?.movieId
            }.await()

    override suspend fun storeMovie(movie: Movie.ForCard) =
        repositoryScope.launch {
            val oldMovie = dao.fetchMovieByTmdbId(movie.searchData.tmdbId)
            // first insert the movie itself
            val movieId =
                if (oldMovie != null) {
                    // if movie was already saved
                    // update it while keeping the old app data
                    // but unarchive it if it was archived
                    val mappedOldMovie: Movie.ForCard = dbMapper.toCardMovie(oldMovie)
                    val movieToSave =
                        dbMapper.toDbMovie(
                            movie.copy(
                                appData =
                                    mappedOldMovie.appData.copy(isArchived = false),
                            ),
                        )
                    dao.updateMovie(movieToSave)
                    movieToSave.movieId
                } else {
                    dao.insertMovie(
                        dbMapper.toDbMovie(
                            movie.copy(
                                appData = movie.appData.copy(movieId = MovieData.NEW_ID),
                            ),
                        ),
                    )
                }

            // then insert the cast and crew
            val staff = movie.staffData.cast + movie.staffData.crew
            val people: List<DbPerson> = dbMapper.toDbPeople(staff)
            val peopleIds = dao.insertPeople(people)
            val savedStaff =
                staff.mapIndexed { index, person ->
                    person.copy(personId = peopleIds[index])
                }

            // finally insert the roles
            val roles: List<DbRole> = dbMapper.toDbRoles(movieId, savedStaff)
            dao.insertRoles(roles)
        }

    override suspend fun updateWatchDates(
        movieId: Long,
        watchDates: List<Long>,
    ) {
        repositoryScope
            .launch {
                dao.replaceWatchDates(movieId, dbMapper.toDbWatchDates(watchDates))
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
