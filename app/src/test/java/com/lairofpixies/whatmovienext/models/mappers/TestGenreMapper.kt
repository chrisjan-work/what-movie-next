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
import io.mockk.every
import io.mockk.mockk

fun testGenreMapper(): GenreMapper = GenreMapper(testGenreMapperContext())

fun testGenreMapperContext(): Context =
    mockk {
        val fixedMap =
            mapOf(
                R.string.genre_28 to "Action",
                R.string.genre_12 to "Adventure",
                R.string.genre_16 to "Animation",
                R.string.genre_35 to "Comedy",
                R.string.genre_80 to "Crime",
                R.string.genre_99 to "Documentary",
                R.string.genre_18 to "Drama",
                R.string.genre_10751 to "Family",
                R.string.genre_14 to "Fantasy",
                R.string.genre_36 to "History",
                R.string.genre_27 to "Horror",
                R.string.genre_10402 to "Music",
                R.string.genre_9648 to "Mystery",
                R.string.genre_10749 to "Romance",
                R.string.genre_878 to "Science Fiction",
                R.string.genre_10770 to "TV Movie",
                R.string.genre_53 to "Thriller",
                R.string.genre_10752 to "War",
                R.string.genre_37 to "Western",
            )

        every { getString(any()) } answers {
            firstArg<Int>().let { fixedMap[it] } ?: ""
        }
    }
