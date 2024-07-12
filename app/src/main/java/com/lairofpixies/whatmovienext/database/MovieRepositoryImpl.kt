package com.lairofpixies.whatmovienext.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieRepositoryImpl(
    private val dao: MovieDao,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
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

    override suspend fun fetchMovieById(movieId: Long): Movie? =
        repositoryScope
            .async {
                dao.fetchMovieById(movieId)
            }.await()

    override suspend fun fetchMoviesByTitle(movieTitle: String): List<Movie> =
        repositoryScope
            .async {
                dao.fetchMoviesByTitle(movieTitle)
            }.await()

    override suspend fun addMovie(movie: Movie): Long =
        repositoryScope
            .async {
                dao.insertMovie(movie)
            }.await()

    override suspend fun updateMovie(movie: Movie): Long {
        repositoryScope
            .launch {
                dao.updateMovieDetails(movie)
            }.join()
        return movie.id
    }

    override suspend fun setWatchState(
        movieId: Long,
        watchState: WatchState,
    ) {
        repositoryScope
            .launch {
                dao.updateWatchState(movieId, watchState)
            }
    }

    override suspend fun archiveMovie(movieId: Long) {
        repositoryScope
            .launch {
                dao.archive(movieId)
            }
    }
}
