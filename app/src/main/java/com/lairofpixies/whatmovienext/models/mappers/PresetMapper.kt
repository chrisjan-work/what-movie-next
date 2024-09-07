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
import com.lairofpixies.whatmovienext.views.state.WordFilter
import com.lairofpixies.whatmovienext.views.state.WordIdFilter
import javax.inject.Inject

class PresetMapper
    @Inject
    constructor(
        private val genreMapper: GenreMapper,
    ) {
        fun toPreset(dbPreset: DbPreset): Preset =
            with(dbPreset) {
                Preset(
                    presetId = presetId,
                    presetName = name,
                    listFilters =
                        ListFilters(
                            listMode = listMode,
                            year = MinMaxFilter(minYear, maxYear, yearEnabled),
                            runtime = MinMaxFilter(minRuntime, maxRuntime, runtimeEnabled),
                            rtScore = MinMaxFilter(minRtScore, maxRtScore, rtScoreEnabled),
                            mcScore = MinMaxFilter(minMcScore, maxMcScore, mcScoreEnabled),
                            genres = WordIdFilter(genreMapper.toGenreIds(genres), genresEnabled),
                            directors = WordFilter(directors.decodeToList(), directorsEnabled),
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
                    yearEnabled = listFilters.year.isEnabled,
                    minRuntime = listFilters.runtime.min,
                    maxRuntime = listFilters.runtime.max,
                    runtimeEnabled = listFilters.runtime.isEnabled,
                    minRtScore = listFilters.rtScore.min,
                    maxRtScore = listFilters.rtScore.max,
                    rtScoreEnabled = listFilters.rtScore.isEnabled,
                    minMcScore = listFilters.mcScore.min,
                    maxMcScore = listFilters.mcScore.max,
                    mcScoreEnabled = listFilters.mcScore.isEnabled,
                    genres = genreMapper.toDbGenreIds(listFilters.genres.wordIds),
                    genresEnabled = listFilters.genres.isEnabled,
                    directors = listFilters.directors.words.encodeToString(),
                    directorsEnabled = listFilters.directors.isEnabled,
                )
            }

        fun runtimeToInput(runtime: Int?): String =
            when (runtime) {
                null -> "-"
                0 -> "0"
                in 1..59 -> "$runtime min"
                else -> "${runtime / 60}h ${runtime % 60}min"
            }

        fun runtimeToButton(runtime: Int?): String =
            when (runtime) {
                null -> ""
                0 -> "0m"
                in 1..59 -> "${runtime}m"
                else -> "${runtime / 60}h${runtime % 60}m"
            }

        fun inputToRuntime(runtimeInput: String): Int? =
            run {
                if (runtimeInput.isEmpty()) return@run null

                // decimal hours
                val decimalHourRegex = """(\d+(?:\.\d+)?)\s*h$""".toRegex(RegexOption.IGNORE_CASE)
                decimalHourRegex.find(runtimeInput)?.let { matchResult ->
                    val decimalHours = matchResult.groupValues[1].toDouble()
                    val hours = decimalHours.toInt()
                    val minutes = ((decimalHours - hours) * 60).toInt()
                    return@run hours * 60 + minutes
                }

                // hours + minutes , or just minutes
                val regex =
                    """(?:(\d+)\s*[hH:])?\s*(?:(\d+)(?:m|M|MIN)?)?""".toRegex(RegexOption.IGNORE_CASE)
                val matchResult = regex.find(runtimeInput)

                val hours = matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val minutes = matchResult?.groupValues?.get(2)?.toIntOrNull() ?: 0

                return@run if (hours == 0 && minutes == 0) {
                    runtimeInput.toIntOrNull()
                } else {
                    hours * 60 + minutes
                }
            }?.coerceIn(VALID_RUNTIME_RANGE)

        fun numberToInput(year: Int?) = year?.toString() ?: "-"

        fun numberToButton(year: Int?) = year?.toString() ?: ""

        fun inputToYear(yearInput: String): Int? {
            val year = yearInput.trim().toIntOrNull() ?: return null
            return when {
                year == 0 -> 0
                year < 100 -> 1900 + year
                else -> year.coerceIn(VALID_YEAR_RANGE)
            }
        }

        fun inputToScore(scoreInput: String): Int? = scoreInput.trim().toIntOrNull()?.coerceIn(VALID_SCORE_RANGE)

        companion object {
            val VALID_RUNTIME_RANGE = 0..60 * 24
            val VALID_YEAR_RANGE = 1900..2100
            val VALID_SCORE_RANGE = 0..100
        }
    }
