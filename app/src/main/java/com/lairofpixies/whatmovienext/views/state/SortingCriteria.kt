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
package com.lairofpixies.whatmovienext.views.state

import androidx.annotation.StringRes
import com.lairofpixies.whatmovienext.R

enum class SortingCriteria(
    @StringRes val display: Int,
    @StringRes val readable: Int? = null,
) {
    CreationTime(R.string.by_date_added),
    Title(R.string.by_title),
    Year(R.string.by_year),
    WatchCount(R.string.by_seen_state, R.string.sort_by_seen_or_not),
    Genre(R.string.by_genre),
    Runtime(R.string.by_runtime),
    Director(R.string.by_director),
    MeanRating(R.string.by_rating, R.string.sort_by_average_rating),
    Random(R.string.shuffle, R.string.shuffle),
    ;

    companion object {
        val Default = CreationTime
    }
}

enum class SortingDirection {
    Ascending,
    Descending,
    ;

    fun opposite() = if (this == Ascending) Descending else Ascending

    companion object {
        val Default = Ascending
    }
}

data class SortingSetup(
    val criteria: SortingCriteria,
    val direction: SortingDirection,
) {
    companion object {
        val Default = SortingSetup(SortingCriteria.Default, SortingDirection.Default)
    }
}
