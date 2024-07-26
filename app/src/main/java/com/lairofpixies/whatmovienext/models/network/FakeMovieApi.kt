package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.RemoteMovie

class FakeMovieApi : MovieApi {
    override suspend fun findMoviesByTitle(title: String): List<RemoteMovie> = FakeResponse.Single.getIt()

    enum class FakeResponse(
        val getIt: () -> List<RemoteMovie>,
    ) {
        Single({ listOf(RemoteMovie(title = "Fake Movie")) }),
        Multiple({
            listOf(
                RemoteMovie(title = "Fake Movie 1"),
                RemoteMovie(title = "Fake Movie 2"),
                RemoteMovie(title = "Fake Movie 3"),
            )
        }),
        Empty({ emptyList() }),
        Error({ throw Exception() }),
    }
}
