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

import com.lairofpixies.whatmovienext.models.data.Preset
import com.lairofpixies.whatmovienext.models.database.data.DbPreset
import com.lairofpixies.whatmovienext.util.decodeToList
import com.lairofpixies.whatmovienext.util.encodeToString
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import javax.inject.Inject

class PresetMapper
    @Inject
    constructor() {
        fun toPreset(dbPreset: DbPreset): Preset =
            with(dbPreset) {
                Preset(
                    presetId = presetId,
                    presetName = name,
                    listFilters =
                        ListFilters(
                            listMode = listMode,
                            year = MinMaxFilter(minYear, maxYear),
                            runtime = MinMaxFilter(minRuntime, maxRuntime),
                            rtScore = MinMaxFilter(minRtScore, maxRtScore),
                            mcScore = MinMaxFilter(minMcScore, maxMcScore),
                            genres = genres.decodeToList(),
                            directors = directors.decodeToList(),
                        ),
                    sortingSetup = SortingSetup(sortingCriteria, sortingDirection),
                )
            }

        fun toDbPreset(preset: Preset): DbPreset =
            with(preset) {
                DbPreset(
                    presetId = presetId,
                    name = presetName,
                    sortingCriteria = sortingSetup.criteria,
                    sortingDirection = sortingSetup.direction,
                    listMode = listFilters.listMode,
                    minYear = listFilters.year.min,
                    maxYear = listFilters.year.max,
                    minRuntime = listFilters.runtime.min,
                    maxRuntime = listFilters.runtime.max,
                    minRtScore = listFilters.rtScore.min,
                    maxRtScore = listFilters.rtScore.max,
                    minMcScore = listFilters.mcScore.min,
                    maxMcScore = listFilters.mcScore.max,
                    genres = listFilters.genres.encodeToString(),
                    directors = listFilters.directors.encodeToString(),
                )
            }
    }
