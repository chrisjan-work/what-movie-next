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

import com.lairofpixies.whatmovienext.models.network.data.TmdbConfiguration
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbSearchResults
import javax.inject.Inject

class TestTmdbApi
    @Inject
    constructor() : TmdbApi {
        private lateinit var fakeResponse: () -> List<TmdbMovieBasic>

        init {
            clearFakeResponse()
        }

        fun clearFakeResponse() {
            fakeResponse = { emptyList() }
        }

        fun replaceFakeResponse(newFakeResponse: () -> List<TmdbMovieBasic>) {
            fakeResponse = newFakeResponse
        }

        fun appendToFakeResponse(vararg movie: TmdbMovieBasic) {
            val newList = fakeResponse() + movie
            fakeResponse = { newList }
        }

        enum class FakeResponse(
            val getIt: () -> List<TmdbMovieBasic>,
        ) {
            Single({ listOf(TmdbMovieBasic(tmdbId = 1, title = "Fake Movie")) }),
            Multiple({
                listOf(
                    TmdbMovieBasic(tmdbId = 1, title = "Fake Movie 1"),
                    TmdbMovieBasic(tmdbId = 2, title = "Fake Movie 2"),
                    TmdbMovieBasic(tmdbId = 3, title = "Fake Movie 3"),
                )
            }),
            Empty({ emptyList() }),
            Error({ throw Exception() }),
        }

        override suspend fun findMoviesByTitle(escapedTitle: String): TmdbSearchResults = TmdbSearchResults(results = fakeResponse())

        override suspend fun getConfiguration(): TmdbConfiguration =
            TmdbConfiguration(
                images =
                    TmdbConfiguration.Images(
                        url = "localhost",
                        sizes = listOf("microscopic", "unremarkable", "humongous"),
                    ),
            )
    }
