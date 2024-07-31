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
import com.lairofpixies.whatmovienext.models.data.Movie.Companion.NEW_ID
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import java.lang.NumberFormatException
import javax.inject.Inject

class RemoteMapper
    @Inject
    constructor(
        private val configRepo: ConfigRepository,
        private val genreRepository: GenreRepository,
    ) {
        fun toMovie(tmdbMovieBasic: TmdbMovieBasic): Movie =
            with(tmdbMovieBasic) {
                Movie(
                    id = NEW_ID,
                    tmdbId = tmdbId,
                    title = title,
                    originalTitle = originalTitle,
                    year = extractYear(releaseDate),
                    thumbnailUrl = configRepo.getThumbnailUrl(posterPath),
                    coverUrl = configRepo.getCoverUrl(posterPath),
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
    }

fun extractYear(releaseDate: String?): Int? =
    if (!releaseDate.isNullOrBlank()) {
        try {
            Regex("(\\d{4})-\\d{2}-\\d{2}")
                .find(releaseDate)
                ?.groupValues
                ?.get(1)
                ?.toInt()
        } catch (e: StringIndexOutOfBoundsException) {
            null
        } catch (e: NumberFormatException) {
            null
        }
    } else {
        null
    }
