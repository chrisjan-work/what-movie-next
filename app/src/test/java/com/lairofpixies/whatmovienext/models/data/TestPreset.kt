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

import com.lairofpixies.whatmovienext.models.database.data.DbPreset
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup

object TestPreset {
    fun forApp() =
        Preset(
            presetId = 37,
            presetName = "preset 37",
            sortingSetup = SortingSetup(SortingCriteria.Year, SortingDirection.Descending),
            listFilters =
                ListFilters(
                    listMode = ListMode.PENDING,
                    year = MinMaxFilter(1990, 2010),
                    runtime = MinMaxFilter(90, 130),
                    rtScore = MinMaxFilter(50, 100),
                    mcScore = MinMaxFilter(60, 80),
                ),
        )

    fun forDb() =
        DbPreset(
            presetId = 37,
            name = "preset 37",
            sortingCriteria = SortingCriteria.Year,
            sortingDirection = SortingDirection.Descending,
            listMode = ListMode.PENDING,
            minYear = 1990,
            maxYear = 2010,
            minRuntime = 90,
            maxRuntime = 130,
            minRtScore = 50,
            maxRtScore = 100,
            minMcScore = 60,
            maxMcScore = 80,
        )
}
