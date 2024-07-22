package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.DownloadMovieInfo
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

    override fun findMoviesByTitle(title: String): StateFlow<DownloadMovieInfo> =
        flow {
            try {
                val movies =
                    repositoryScope
                        .async {
                            movieApi.findMoviesByTitle(title)
                        }.await()

                val downloadMovieInfo =
                    when (movies.size) {
                        0 -> DownloadMovieInfo.Empty
                        1 ->
                            DownloadMovieInfo.Single(
                                MovieMapper.mapNetToApp(movies.first()),
                            )

                        else ->
                            DownloadMovieInfo.Multiple(
                                movies.map { backendMovie ->
                                    MovieMapper.mapNetToApp(backendMovie)
                                },
                            )
                    }

                emit(downloadMovieInfo)
            } catch (httpException: HttpException) {
                emit(DownloadMovieInfo.Failed(httpException))
            } catch (exception: Exception) {
                emit(DownloadMovieInfo.Failed(exception))
            }
        }.stateIn(
            repositoryScope,
            SharingStarted.Eagerly,
            initialValue = DownloadMovieInfo.Loading,
        )
}
