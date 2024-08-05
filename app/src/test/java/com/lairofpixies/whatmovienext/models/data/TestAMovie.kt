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
package com.lairofpixies.whatmovienext.models.data

import com.lairofpixies.whatmovienext.models.data.MovieData.UNKNOWN_ID

object TestAMovie {
    fun forSearch(
        title: String,
        tmdbId: Long = UNKNOWN_ID,
        originalTitle: String = "",
        year: Int? = null,
        thumbnailUrl: String = "",
        coverUrl: String = "",
        genres: List<String> = emptyList(),
    ) = AMovie.ForSearch(
        searchData =
            MovieData.SearchData(
                title = title,
                tmdbId = tmdbId,
                originalTitle = originalTitle,
                year = year,
                thumbnailUrl = thumbnailUrl,
                coverUrl = coverUrl,
                genres = genres,
            ),
    )

    fun forCard(
        id: Long = 1,
        watchState: WatchState = WatchState.PENDING,
        isArchived: Boolean = false,
        tmdbId: Long = 1,
        title: String = "",
        originalTitle: String = "",
        year: Int = 0,
        thumbnailUrl: String = "",
        coverUrl: String = "",
        genres: List<String> = emptyList(),
        imdbId: String = "",
        tagline: String = "",
        plot: String = "",
        runtimeMinutes: Int = 0,
    ) = AMovie.ForCard(
        appData =
            MovieData.AppData(
                id = id,
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
                genres = genres,
            ),
        detailData =
            MovieData.DetailData(
                imdbId = imdbId,
                tagline = tagline,
                plot = plot,
                runtimeMinutes = runtimeMinutes,
            ),
        staffData = MovieData.StaffData(),
    )
}
