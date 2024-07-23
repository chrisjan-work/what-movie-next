package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.mappers.MovieMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import retrofit2.HttpException

class ApiRepositoryImpl(
    private val movieApi: MovieApi,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ApiRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun findMoviesByTitle(title: String): StateFlow<AsyncMovieInfo> =
        flow {
            try {
                val remoteMovies =
                    repositoryScope
                        .async {
                            movieApi.findMoviesByTitle(title)
                        }.await()

                val asyncMovieInfo =
                    AsyncMovieInfo.fromList(
                        remoteMovies.map { remoteMovie ->
                            MovieMapper.mapNetToApp(remoteMovie)
                        },
                    )
                emit(asyncMovieInfo)
            } catch (httpException: HttpException) {
                emit(AsyncMovieInfo.Failed(httpException))
            } catch (exception: Exception) {
                emit(AsyncMovieInfo.Failed(exception))
            }
        }.stateIn(
            repositoryScope,
            SharingStarted.Eagerly,
            initialValue = AsyncMovieInfo.Loading,
        )
}
