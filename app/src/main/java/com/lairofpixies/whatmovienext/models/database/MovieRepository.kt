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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MovieRepository {
    // read
    val movies: Flow<AsyncMovieInfo>

    val archivedMovies: Flow<AsyncMovieInfo>

    fun singleMovie(movieId: Long): StateFlow<AsyncMovieInfo>

    suspend fun fetchMovieById(movieId: Long): Movie?

    suspend fun fetchMoviesByTitle(movieTitle: String): List<Movie>

    // write
    suspend fun addMovie(movie: Movie): Long

    suspend fun updateMovie(movie: Movie): Long

    suspend fun setWatchState(
        movieId: Long,
        watchState: WatchState,
    )

    // delete
    suspend fun archiveMovie(movieId: Long)

    suspend fun restoreMovie(movieId: Long)

    suspend fun deleteMovie(movieId: Long)
}
