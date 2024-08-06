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

import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.MovieData
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.util.decodeToList
import com.lairofpixies.whatmovienext.util.encodeToString
import javax.inject.Inject

class DbMapper
    @Inject
    constructor() {
        fun toCardMovie(dbMovie: DbMovie): Movie.ForCard =
            with(dbMovie) {
                Movie.ForCard(
                    appData =
                        MovieData.AppData(
                            id = id,
                            creationTime = creationTime,
                            watchState = watchState,
                            isArchived = isArchived,
                        ),
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId,
                            title = title,
                            originalTitle = originalTitle,
                            year = year,
                            thumbnailUrl = thumbnailUrl,
                            coverUrl = coverUrl,
                            genres = toGenres(genres),
                        ),
                    detailData =
                        MovieData.DetailData(
                            imdbId = imdbId,
                            tagline = tagline,
                            plot = plot,
                            runtimeMinutes = runtimeMinutes,
                        ),
                    staffData =
                        MovieData.StaffData(
                            // TODO
                        ),
                )
            }

        fun toListMovie(dbMovie: DbMovie): Movie.ForList =
            with(dbMovie) {
                Movie.ForList(
                    appData =
                        MovieData.AppData(
                            id = id,
                            creationTime = creationTime,
                            watchState = watchState,
                            isArchived = isArchived,
                        ),
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId,
                            title = title,
                            originalTitle = originalTitle,
                            year = year,
                            thumbnailUrl = thumbnailUrl,
                            coverUrl = coverUrl,
                            genres = toGenres(genres),
                        ),
                    detailData =
                        MovieData.DetailData(
                            imdbId = imdbId,
                            tagline = tagline,
                            plot = plot,
                            runtimeMinutes = runtimeMinutes,
                        ),
                )
            }

        fun toDbMovie(cardMovie: Movie.ForCard): DbMovie =
            with(cardMovie) {
                DbMovie(
                    id = appData.id,
                    creationTime = appData.creationTime,
                    tmdbId = searchData.tmdbId,
                    imdbId = detailData.imdbId,
                    title = searchData.title,
                    originalTitle = searchData.originalTitle,
                    year = searchData.year,
                    thumbnailUrl = searchData.thumbnailUrl,
                    coverUrl = searchData.coverUrl,
                    tagline = detailData.tagline,
                    plot = detailData.plot,
                    genres = toDbGenres(searchData.genres),
                    runtimeMinutes = detailData.runtimeMinutes,
                    watchState = appData.watchState,
                    isArchived = appData.isArchived,
                )
            }

        fun toGenres(dbGenres: String): List<String> = dbGenres.decodeToList()

        fun toDbGenres(genres: List<String>): String = genres.encodeToString()
    }
