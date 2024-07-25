package com.lairofpixies.whatmovienext.models.database

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
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

    override val movies: Flow<AsyncMovieInfo> =
        dao
            .getAllMovies()
            .map { AsyncMovieInfo.fromList(it) }
            .flowOn(ioDispatcher)

    override val archivedMovies: Flow<AsyncMovieInfo> =
        dao
            .getArchivedMovies()
            .map { AsyncMovieInfo.fromList(it) }
            .flowOn(ioDispatcher)

    override fun singleMovie(movieId: Long): StateFlow<AsyncMovieInfo> =
        dao
            .getMovie(movieId)
            .map { it?.let { AsyncMovieInfo.Single(it) } ?: AsyncMovieInfo.Empty }
            .stateIn(
                repositoryScope,
                SharingStarted.Eagerly,
                initialValue = AsyncMovieInfo.Loading,
            )

    override suspend fun fetchMovieById(movieId: Long): Movie? =
        repositoryScope
            .async {
                if (movieId != Movie.NEW_ID) {
                    dao.fetchMovieById(movieId)
                } else {
                    null
                }
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
                dao.updateMovie(movie)
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

    override suspend fun restoreMovie(movieId: Long) {
        repositoryScope
            .launch {
                dao.restore(movieId)
            }
    }

    override suspend fun deleteMovie(movie: Movie) {
        repositoryScope
            .launch {
                dao.delete(movie)
            }
    }
}
