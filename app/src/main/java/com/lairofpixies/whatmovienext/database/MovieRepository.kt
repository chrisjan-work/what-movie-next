package com.lairofpixies.whatmovienext.database

import kotlinx.coroutines.flow.StateFlow

interface MovieRepository {
    val movies: StateFlow<List<Movie>>

    fun addMovie(title: String)

    fun setWatchState(
        movieId: Int,
        watchState: WatchState,
    )

    fun deleteMovie(movie: Movie)
}
