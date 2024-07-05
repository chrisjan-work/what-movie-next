package com.lairofpixies.whatmovienext.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieRepositoryImpl(
    private val dao: MovieDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MovieRepository {
    override val movies: StateFlow<List<Movie>> =
        dao
            .getAllMovies()
            .stateIn(
                CoroutineScope(ioDispatcher),
                SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    override fun getMovie(movieId: Int): StateFlow<PartialMovie> =
        dao
            .getMovie(movieId)
            .map { it?.let { PartialMovie.Completed(it) } ?: PartialMovie.NotFound }
            .stateIn(
                CoroutineScope(ioDispatcher),
                SharingStarted.Eagerly,
                initialValue = PartialMovie.Loading,
            )

    override fun addMovie(title: String) {
        CoroutineScope(ioDispatcher).launch {
            dao.insertMovies(listOf(Movie(title = title, watchState = WatchState.PENDING)))
        }
    }

    override fun setWatchState(
        movieId: Int,
        watchState: WatchState,
    ) {
        CoroutineScope(ioDispatcher).launch {
            dao.updateWatchState(movieId, watchState)
        }
    }

    override fun archiveMovie(movieId: Int) {
        CoroutineScope(ioDispatcher).launch {
            dao.archive(movieId)
        }
    }
}
