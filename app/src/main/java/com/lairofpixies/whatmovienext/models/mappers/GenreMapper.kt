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

import android.content.Context
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.util.decodeToList
import com.lairofpixies.whatmovienext.util.encodeToString
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GenreMapper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun toGenreNames(genreIds: List<Long>): List<String> =
            genreIds.mapNotNull { genreId ->
                genreMap[genreId]?.let { genreRes -> context.getString(genreRes) }
            }

        fun toDbGenreIds(genreIds: List<Long>): String = genreIds.map { it.toString() }.encodeToString()

        fun toGenreIds(dbGenreIds: String): List<Long> = dbGenreIds.decodeToList().mapNotNull { it.toLongOrNull() }

        fun allGenreNamesMap(): Map<Long, String> = genreMap.mapValues { context.getString(it.value) }

        private val genreMap =
            mapOf(
                28L to R.string.genre_28,
                12L to R.string.genre_12,
                16L to R.string.genre_16,
                35L to R.string.genre_35,
                80L to R.string.genre_80,
                99L to R.string.genre_99,
                18L to R.string.genre_18,
                10751L to R.string.genre_10751,
                14L to R.string.genre_14,
                36L to R.string.genre_36,
                27L to R.string.genre_27,
                10402L to R.string.genre_10402,
                9648L to R.string.genre_9648,
                10749L to R.string.genre_10749,
                878L to R.string.genre_878,
                10770L to R.string.genre_10770,
                53L to R.string.genre_53,
                10752L to R.string.genre_10752,
                37L to R.string.genre_37,
            )
    }
