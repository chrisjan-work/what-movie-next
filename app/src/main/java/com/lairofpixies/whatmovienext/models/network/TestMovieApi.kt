package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.RemoteMovie
import javax.inject.Inject

// TODO: move to the test module
class TestMovieApi
    @Inject
    constructor() : MovieApi {
        private lateinit var fakeResponse: () -> List<RemoteMovie>

        init {
            clearFakeResponse()
        }

        fun clearFakeResponse() {
            fakeResponse = { emptyList() }
        }

        fun replaceFakeResponse(newFakeResponse: () -> List<RemoteMovie>) {
            fakeResponse = newFakeResponse
        }

        fun appendToFakeResponse(vararg movie: RemoteMovie) {
            val newList = fakeResponse() + movie
            fakeResponse = { newList }
        }

        override suspend fun findMoviesByTitle(title: String): List<RemoteMovie> = fakeResponse()

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
