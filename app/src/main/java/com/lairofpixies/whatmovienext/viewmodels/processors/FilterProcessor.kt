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
package com.lairofpixies.whatmovienext.viewmodels.processors

import androidx.annotation.VisibleForTesting
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import com.lairofpixies.whatmovienext.views.state.WordFilter
import com.lairofpixies.whatmovienext.views.state.WordIdFilter
import javax.inject.Inject

class FilterProcessor
    @Inject
    constructor() {
        fun filterMovies(
            movies: AsyncMovie,
            listFilters: ListFilters,
        ): AsyncMovie {
            val filtered =
                movies
                    .toList<Movie.ForList>()
                    .toMutableList()
                    .apply {
                        with(listFilters) {
                            byListMode(listMode)
                            byYear(year)
                            byNumber(runtime) { it.detailData.runtimeMinutes }
                            byNumber(rtScore) { it.detailData.rtRating.percentValue }
                            byNumber(mcScore) { it.detailData.mcRating.percentValue }
                            byGenreId(genres) { it.searchData.genreIds }
                            byText(directors) { it.detailData.directorNames }
                        }
                    }

            return AsyncMovie.fromList(filtered)
        }

        private fun <T> MutableList<T>.filterInPlace(sieve: (T) -> Boolean) = removeIf { !sieve(it) }

        @VisibleForTesting
        fun MutableList<Movie.ForList>.byListMode(listMode: ListMode) {
            when (listMode) {
                ListMode.ALL -> {}
                ListMode.WATCHED -> filterInPlace { it.appData.watchDates.isNotEmpty() }
                ListMode.PENDING -> filterInPlace { it.appData.watchDates.isEmpty() }
            }
        }

        @VisibleForTesting
        fun MutableList<Movie.ForList>.byNumber(
            criteria: MinMaxFilter,
            getValue: (Movie.ForList) -> Int,
        ) {
            if (criteria.isActive) {
                filterInPlace { movie ->
                    val value = getValue(movie)
                    val minInclusive = criteria.min ?: value
                    val maxInclusive = criteria.max ?: value
                    return@filterInPlace value in minInclusive..maxInclusive
                }
            }
        }

        @VisibleForTesting
        fun MutableList<Movie.ForList>.byYear(criteria: MinMaxFilter) {
            if (criteria.isActive) {
                filterInPlace { movie ->
                    val value = movie.searchData.year ?: return@filterInPlace true
                    val minInclusive = criteria.min ?: value
                    val maxInclusive = criteria.max ?: value
                    return@filterInPlace value in minInclusive..maxInclusive
                }
            }
        }

        @VisibleForTesting
        fun MutableList<Movie.ForList>.byText(
            wordFilter: WordFilter,
            getValue: (Movie.ForList) -> List<String>,
        ) {
            if (wordFilter.isActive) {
                filterInPlace { movie ->
                    val offering = getValue(movie)
                    return@filterInPlace wordFilter.words.any { it in offering }
                }
            }
        }

        @VisibleForTesting
        fun MutableList<Movie.ForList>.byGenreId(
            wordFilter: WordIdFilter,
            getValue: (Movie.ForList) -> List<Long>,
        ) {
            if (wordFilter.isActive) {
                filterInPlace { movie ->
                    val offering = getValue(movie)
                    return@filterInPlace wordFilter.wordIds.any { it in offering }
                }
            }
        }
    }
