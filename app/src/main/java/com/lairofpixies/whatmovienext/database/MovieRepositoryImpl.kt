package com.lairofpixies.whatmovienext.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieRepositoryImpl(
    private val dao: MovieDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MovieRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override val movies: Flow<List<Movie>> = dao.getAllMovies().flowOn(ioDispatcher)

    override fun getMovie(movieId: Long): StateFlow<PartialMovie> =
        dao
            .getMovie(movieId)
            .map { it?.let { PartialMovie.Completed(it) } ?: PartialMovie.NotFound }
            .stateIn(
                repositoryScope,
                SharingStarted.Eagerly,
                initialValue = PartialMovie.Loading,
            )

    override suspend fun addMovie(title: String) {
        repositoryScope
            .launch {
                dao.insertMovie(Movie(title = title, watchState = WatchState.PENDING))
            }.join()
    }

    override suspend fun setWatchState(
        movieId: Long,
        watchState: WatchState,
    ) {
        repositoryScope
            .launch {
                dao.updateWatchState(movieId, watchState)
            }.join()
    }

    override suspend fun archiveMovie(movieId: Long) {
        repositoryScope
            .launch {
                dao.archive(movieId)
            }.join()
    }
}
