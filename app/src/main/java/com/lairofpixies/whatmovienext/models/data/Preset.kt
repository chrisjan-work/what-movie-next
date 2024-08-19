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

import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup

data class Preset(
    val presetId: Long = NEW_ID,
    val presetName: String = "",
    val listFilters: ListFilters,
    val sortingSetup: SortingSetup,
) {
    companion object {
        const val NEW_ID = 0L
        const val FIXED_ID = 1L
        val Default =
            Preset(
                presetId = FIXED_ID,
                presetName = "default",
                listFilters = ListFilters(ListMode.ALL),
                sortingSetup =
                    SortingSetup(
                        SortingCriteria.CreationTime,
                        SortingDirection.Ascending,
                    ),
            )
    }
}
