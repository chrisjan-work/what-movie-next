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
package com.lairofpixies.whatmovienext.models.mappers

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.util.decodeToList
import com.lairofpixies.whatmovienext.util.encodeToString
import javax.inject.Inject

class DbMapper
    @Inject
    constructor() {
        fun toMovie(dbMovie: DbMovie): Movie =
            with(dbMovie) {
                Movie(
                    id = id,
                    tmdbId = tmdbId,
                    title = title,
                    originalTitle = originalTitle,
                    year = year,
                    thumbnailUrl = thumbnailUrl,
                    coverUrl = coverUrl,
                    summary = summary,
                    genres = toGenres(genres),
                    watchState = watchState,
                    isArchived = isArchived,
                )
            }

        fun toMovies(dbMovies: List<DbMovie>): List<Movie> = dbMovies.map { toMovie(it) }

        fun toAsyncMovies(dbMovies: List<DbMovie>): AsyncMovieInfo = AsyncMovieInfo.fromList(toMovies(dbMovies))

        fun toDbMovie(movie: Movie): DbMovie =
            with(movie) {
                DbMovie(
                    id = id,
                    tmdbId = tmdbId,
                    title = title,
                    originalTitle = originalTitle,
                    year = year,
                    thumbnailUrl = thumbnailUrl,
                    coverUrl = coverUrl,
                    summary = summary,
                    genres = toDbGenres(genres),
                    watchState = watchState,
                    isArchived = isArchived,
                )
            }

        fun toGenres(dbGenres: String): List<String> = dbGenres.decodeToList()

        fun toDbGenres(genres: List<String>): String = genres.encodeToString()
    }
