package com.lairofpixies.whatmovienext.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MovieRepository {
    // read
    val movies: Flow<List<Movie>>

    fun getMovie(movieId: Long): StateFlow<PartialMovie>

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
}
