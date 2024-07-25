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

    suspend fun deleteMovie(movie: Movie)
}
