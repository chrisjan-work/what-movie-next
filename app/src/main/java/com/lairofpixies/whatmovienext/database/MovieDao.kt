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

    @Query("SELECT * FROM movie WHERE isArchived = 0")
    fun getAllMovies(): Flow<List<Movie>>

    @Query("UPDATE movie SET watchState = :watchState WHERE id = :id")
    fun updateWatchState(
        id: Int,
        watchState: WatchState,
    )

    @Query("UPDATE movie SET isArchived = 1 WHERE id = :id")
    fun archive(id: Int)
}
