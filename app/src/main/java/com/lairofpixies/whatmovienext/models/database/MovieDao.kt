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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie WHERE isArchived = 0")
    fun getAllMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movie WHERE id = :id")
    fun getMovie(id: Long): Flow<Movie?>

    @Query("SELECT * FROM movie WHERE id = :id")
    suspend fun fetchMovieById(id: Long): Movie?

    @Query("SELECT * FROM movie WHERE UPPER(title) = UPPER(:title)")
    suspend fun fetchMoviesByTitle(title: String): List<Movie>

    @Query("SELECT * FROM movie WHERE isArchived = 1")
    fun getArchivedMovies(): Flow<List<Movie>>

    @Insert
    suspend fun insertMovie(movie: Movie): Long

    @Insert
    suspend fun insertMovies(movies: List<Movie>)

    @Delete
    suspend fun delete(movie: Movie)

    @Update
    suspend fun updateMovie(movie: Movie)

    @Query("UPDATE movie SET watchState = :watchState WHERE id = :id")
    suspend fun updateWatchState(
        id: Long,
        watchState: WatchState,
    )

    @Query("UPDATE movie SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE movie SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)
}
