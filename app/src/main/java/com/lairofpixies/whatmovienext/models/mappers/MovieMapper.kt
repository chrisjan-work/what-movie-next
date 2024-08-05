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

import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import javax.inject.Inject

class MovieMapper
    @Inject
    constructor(
        private val configRepo: ConfigRepository,
        private val genreRepository: GenreRepository,
    ) {
        // TODO: de-duplicate
        fun toGenreNames(genreIds: List<Long>?): List<String> =
            genreIds?.let { genreRepository.genreNamesByTmdbIds(genreIds) } ?: emptyList()

        // TODO: de-duplicate
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
