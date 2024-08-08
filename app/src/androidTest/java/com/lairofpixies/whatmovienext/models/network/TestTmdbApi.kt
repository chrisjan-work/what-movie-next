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
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import com.lairofpixies.whatmovienext.models.network.data.TmdbSearchResults
import javax.inject.Inject

class TestTmdbApi
    @Inject
    constructor() : TmdbApi {
        lateinit var fakeMoviesBasic: () -> List<TmdbMovieBasic>
        lateinit var fakeMovieExtended: () -> TmdbMovieExtended
        lateinit var fakeGenres: () -> List<TmdbGenres.TmdbGenre>

        init {
            clearFakeResponses()
        }

        fun clearFakeResponses() {
            fakeMoviesBasic = { emptyList() }
            fakeGenres = { emptyList() }
            fakeMovieExtended = { TmdbMovieExtended(success = false) }
        }

        fun appendToFakeMovies(vararg movie: TmdbMovieBasic) {
            val newList = fakeMoviesBasic() + movie
            fakeMoviesBasic = { newList }
        }

        override suspend fun findMoviesByTitle(
            escapedTitle: String,
            page: Int?,
        ): TmdbSearchResults = TmdbSearchResults(results = fakeMoviesBasic())

        override suspend fun getConfiguration(): TmdbConfiguration =
            TmdbConfiguration(
                images =
                    TmdbConfiguration.Images(
                        url = "localhost",
                        posterSizes = listOf("microscopic", "unremarkable", "humongous"),
                        profileSizes = listOf("ant", "whale"),
                    ),
            )

        override suspend fun getGenres(): TmdbGenres = TmdbGenres(genres = fakeGenres())

        override suspend fun getMovieDetails(tmdbId: Long): TmdbMovieExtended = fakeMovieExtended()
    }
