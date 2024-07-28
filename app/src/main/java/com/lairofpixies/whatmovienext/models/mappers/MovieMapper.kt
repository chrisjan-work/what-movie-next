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
import com.lairofpixies.whatmovienext.models.data.remote.RemoteMovieSummary
import com.lairofpixies.whatmovienext.models.network.BackendConfigRepository
import java.lang.NumberFormatException
import javax.inject.Inject

// todo: fetch the genres (if necessary)
class MovieMapper
    @Inject
    constructor(
        private val configRepo: BackendConfigRepository,
    ) {
        fun mapNetToApp(remoteMovieSummary: RemoteMovieSummary) =
            Movie(
                id = NEW_ID,
                tmdbId = remoteMovieSummary.tmdbId,
                title = remoteMovieSummary.title,
                originalTitle = remoteMovieSummary.originalTitle,
                year = extractYear(remoteMovieSummary.releaseDate),
                thumbnailUrl = configRepo.getThumbnailUrl(remoteMovieSummary.posterPath),
                coverUrl = configRepo.getCoverUrl(remoteMovieSummary.posterPath),
            )
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
