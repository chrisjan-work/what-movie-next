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
import com.lairofpixies.whatmovienext.models.data.MovieData.NEW_ID
import com.lairofpixies.whatmovienext.models.data.MovieData.UNKNOWN_ID
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import java.lang.NumberFormatException
import javax.inject.Inject

class RemoteMapper
    @Inject
    constructor(
        private val configRepo: ConfigRepository,
        private val genreRepository: GenreRepository,
    ) {
        fun toSearchMovie(tmdbMovieBasic: TmdbMovieBasic): Movie.ForSearch =
            with(tmdbMovieBasic) {
                Movie.ForSearch(
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId,
                            title = title,
                            originalTitle = originalTitle,
                            year = toYear(releaseDate),
                            thumbnailUrl = configRepo.getThumbnailUrl(posterPath),
                            coverUrl = configRepo.getCoverUrl(posterPath),
                            genres = toGenreNames(genreIds),
                        ),
                )
            }

        fun toCardMovie(tmdbMovieExtended: TmdbMovieExtended): Movie.ForCard =
            with(tmdbMovieExtended) {
                Movie.ForCard(
                    appData =
                        MovieData.AppData(
                            id = NEW_ID,
                            watchState = WatchState.PENDING,
                            isArchived = false,
                        ),
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId ?: UNKNOWN_ID,
                            title = title ?: "",
                            originalTitle = originalTitle ?: "",
                            year = toYear(releaseDate),
                            thumbnailUrl = configRepo.getThumbnailUrl(posterPath),
                            coverUrl = configRepo.getCoverUrl(posterPath),
                            genres = toGenreNames(genres?.map { it.tmdbId }),
                        ),
                    detailData =
                        MovieData.DetailData(
                            imdbId = imdbId,
                            tagline = tagline ?: "",
                            plot = summary ?: "",
                            runtimeMinutes = runtime ?: 0,
                        ),
                    staffData =
                        MovieData.StaffData(
                            // TODO
                        ),
                )
            }

        fun toDbGenres(tmdbGenres: TmdbGenres): List<DbGenre> =
            tmdbGenres.genres.map { tmdbGenre ->
                with(tmdbGenre) {
                    DbGenre(
                        tmdbId = tmdbId,
                        name = name,
                    )
                }
            }

        fun toGenreNames(genreIds: List<Long>?): List<String> =
            genreIds?.let { genreRepository.genreNamesByTmdbIds(genreIds) } ?: emptyList()

        fun toYear(releaseDate: String?): Int? =
            if (!releaseDate.isNullOrBlank()) {
                try {
                    Regex("(\\d{4})-\\d{2}-\\d{2}")
                        .find(releaseDate)
                        ?.groupValues
                        ?.get(1)
                        ?.toInt()
                } catch (_: StringIndexOutOfBoundsException) {
                    null
                } catch (_: NumberFormatException) {
                    null
                }
            } else {
                null
            }
    }
