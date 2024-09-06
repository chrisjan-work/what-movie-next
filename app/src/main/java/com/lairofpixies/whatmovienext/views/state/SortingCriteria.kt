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
import com.lairofpixies.whatmovienext.views.screens.UiTags

enum class SortingCriteria(
    @StringRes val display: Int,
    val tag: String,
    @StringRes val readable: Int? = null,
) {
    CreationTime(R.string.by_date_added, UiTags.Buttons.SORT_BY_CREATION_TIME),
    Title(R.string.by_title, UiTags.Buttons.SORT_BY_TITLE),
    Year(R.string.by_year, UiTags.Buttons.SORT_BY_YEAR),
    WatchCount(R.string.by_seen_state, UiTags.Buttons.SORT_BY_SEEN, R.string.sort_by_seen_or_not),
    Genre(R.string.by_genre, UiTags.Buttons.SORT_BY_GENRE),
    Runtime(R.string.by_runtime, UiTags.Buttons.SORT_BY_RUNTIME),
    Director(R.string.by_director, UiTags.Buttons.SORT_BY_DIRECTOR),
    MeanRating(R.string.by_avg_score, UiTags.Buttons.SORT_BY_SCORE, R.string.sort_by_average_score),
    Random(R.string.shuffle, UiTags.Buttons.SHUFFLE_ACTION, R.string.shuffle),
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
