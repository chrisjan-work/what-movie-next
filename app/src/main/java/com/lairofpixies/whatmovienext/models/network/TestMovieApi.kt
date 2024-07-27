/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
