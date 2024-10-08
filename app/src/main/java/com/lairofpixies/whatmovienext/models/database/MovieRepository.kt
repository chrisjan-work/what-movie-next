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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    // read
    val listedMovies: Flow<AsyncMovie>

    val archivedMovies: Flow<AsyncMovie>

    val isEmpty: Flow<Boolean>

    fun singleCardMovie(movieId: Long): Flow<AsyncMovie>

    fun getAllPeopleNamesByDepartment(department: Departments): Flow<List<String>>

    suspend fun fetchMovieIdFromTmdbId(tmdbId: Long): Long?

    suspend fun retrieveFullMovieDump(): List<Movie.ForCard>

    // write
    suspend fun storeMovie(movie: Movie.ForCard): Job

    suspend fun updateWatchDates(
        movieId: Long,
        watchDates: List<Long>,
    )

    // delete
    suspend fun archiveMovie(movieId: Long)

    suspend fun restoreMovie(movieId: Long)

    suspend fun deleteMovie(movieId: Long)
}
