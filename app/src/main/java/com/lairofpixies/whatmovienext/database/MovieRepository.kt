package com.lairofpixies.whatmovienext.database

import kotlinx.coroutines.flow.StateFlow

interface MovieRepository {
    val movies: StateFlow<List<Movie>>

    fun getMovie(movieId: Int): StateFlow<PartialMovie>

    fun addMovie(title: String)

    fun setWatchState(
        movieId: Int,
        watchState: WatchState,
    )

    fun archiveMovie(movieId: Int)
}
