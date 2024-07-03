package com.lairofpixies.whatmovienext.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert
    suspend fun insertMovies(movies: List<Movie>)

    @Delete
    suspend fun delete(movie: Movie)

    @Query("SELECT * FROM movie")
    fun getAllMovies(): Flow<List<Movie>>
}
