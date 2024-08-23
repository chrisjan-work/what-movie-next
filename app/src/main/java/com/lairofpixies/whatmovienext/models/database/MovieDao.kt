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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.models.database.data.DbPerson
import com.lairofpixies.whatmovienext.models.database.data.DbRole
import com.lairofpixies.whatmovienext.models.database.data.DbStaff
import com.lairofpixies.whatmovienext.models.database.data.DbStaffedMovie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    // Movies
    @Query("SELECT * FROM dbmovie WHERE isArchived = 0")
    fun getAllMovies(): Flow<List<DbMovie>>

    @Query("SELECT * FROM dbmovie WHERE isArchived = 0 LIMIT 1")
    fun getOneMovie(): Flow<DbMovie?>

    @Query("SELECT * FROM dbmovie WHERE movieId = :movieId")
    suspend fun fetchMovieById(movieId: Long): DbMovie?

    @Query("SELECT * FROM dbmovie WHERE UPPER(title) = UPPER(:title)")
    suspend fun fetchMoviesByTitle(title: String): List<DbMovie>

    @Query("SELECT * FROM dbmovie WHERE isArchived = 1")
    fun getArchivedMovies(): Flow<List<DbMovie>>

    @Insert
    suspend fun insertMovie(dbMovie: DbMovie): Long

    @Insert
    suspend fun insertMovies(dbMovies: List<DbMovie>): List<Long>

    @Delete
    suspend fun deleteMovie(dbMovie: DbMovie)

    @Update
    suspend fun updateMovie(dbMovie: DbMovie)

    @Query("UPDATE dbmovie SET dbWatchDates = :dbWatchDates WHERE movieId = :movieId")
    suspend fun replaceWatchDates(
        movieId: Long,
        dbWatchDates: String,
    )

    @Query("UPDATE dbmovie SET isArchived = 1 WHERE movieId = :movieId")
    suspend fun archive(movieId: Long)

    @Query("UPDATE dbmovie SET isArchived = 0 WHERE movieId = :movieId")
    suspend fun restore(movieId: Long)

    // Cast & Crew
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(dbPeople: List<DbPerson>): List<Long>

    @Query("SELECT * FROM dbperson")
    suspend fun fetchAllPeople(): List<DbPerson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(dbRoles: List<DbRole>)

    @Query("SELECT * FROM dbrole")
    suspend fun fetchAllRoles(): List<DbRole>

    @Transaction
    @Query("SELECT * FROM dbmovie WHERE movieId = :movieId")
    fun getStaffedMovie(movieId: Long): Flow<DbStaffedMovie?>

    @Transaction
    @Query("SELECT * FROM dbmovie WHERE tmdbId = :tmdbId")
    suspend fun fetchMovieByTmdbId(tmdbId: Long): DbStaffedMovie?

    @Transaction
    @Query("SELECT * FROM dbrole WHERE dept = :dept")
    fun getStaffByDepartment(dept: String): Flow<List<DbStaff>>
}
