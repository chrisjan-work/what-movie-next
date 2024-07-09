package com.lairofpixies.whatmovienext.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MovieRepository {
    val movies: Flow<List<Movie>>

    fun getMovie(movieId: Int): StateFlow<PartialMovie>

    suspend fun addMovie(title: String)

    suspend fun setWatchState(
        movieId: Int,
        watchState: WatchState,
    )

    suspend fun archiveMovie(movieId: Int)

    suspend fun generateNewMoviePlaceholder(): Int
}
