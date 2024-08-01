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
        private lateinit var fakeMoviesBasic: () -> List<TmdbMovieBasic>
        private lateinit var fakeMovieExtended: () -> TmdbMovieExtended
        private lateinit var fakeGenres: () -> List<TmdbGenres.TmdbGenre>

        init {
            clearFakeMovies()
            clearFakeMovieExtended()
            clearFakeGenres()
        }

        fun clearFakeMovies() {
            fakeMoviesBasic = { emptyList() }
        }

        fun replaceFakeMovies(newFakeMovies: () -> List<TmdbMovieBasic>) {
            fakeMoviesBasic = newFakeMovies
        }

        fun appendToFakeMovies(vararg movie: TmdbMovieBasic) {
            val newList = fakeMoviesBasic() + movie
            fakeMoviesBasic = { newList }
        }

        fun clearFakeGenres() {
            fakeGenres = { emptyList() }
        }

        fun replaceFakeGenres(newFakeGenres: () -> List<TmdbGenres.TmdbGenre>) {
            fakeGenres = newFakeGenres
        }

        fun appendToFakeGenres(vararg genre: TmdbGenres.TmdbGenre) {
            val newList = fakeGenres() + genre
            fakeGenres = { newList }
        }

        fun mapGenreIds(requestedGenres: List<String>): List<Long> {
            val mapped = fakeGenres().associate { genre -> genre.name to genre.tmdbId }
            return requestedGenres.mapNotNull { name -> mapped[name] }
        }

        fun clearFakeMovieExtended() {
            fakeMovieExtended = { TmdbMovieExtended(success = false) }
        }

        fun replaceFakeMovieExtended(newFakeMovieExtended: () -> TmdbMovieExtended) {
            fakeMovieExtended = newFakeMovieExtended
        }

        override suspend fun findMoviesByTitle(escapedTitle: String): TmdbSearchResults = TmdbSearchResults(results = fakeMoviesBasic())

        override suspend fun getConfiguration(): TmdbConfiguration =
            TmdbConfiguration(
                images =
                    TmdbConfiguration.Images(
                        url = "localhost",
                        sizes = listOf("microscopic", "unremarkable", "humongous"),
                    ),
            )

        override suspend fun getGenres(): TmdbGenres = TmdbGenres(genres = fakeGenres())

        override suspend fun getMovieDetails(tmdbId: Long): TmdbMovieExtended = fakeMovieExtended()
    }
