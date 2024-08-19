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

import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import javax.inject.Inject
import kotlin.random.Random

class SortProcessor
    @Inject
    constructor(
        private val randomizer: Random,
    ) {
        fun sortMovies(
            movies: AsyncMovie,
            sortingSetup: SortingSetup,
        ): AsyncMovie {
            val unsorted = movies.toList<Movie.ForList>()
            val sortedAscending =
                when (sortingSetup.criteria) {
                    SortingCriteria.CreationTime ->
                        unsorted.sortedBy { it.appData.creationTime }

                    SortingCriteria.Title ->
                        unsorted.sortedBy { it.searchData.title }

                    SortingCriteria.Year ->
                        unsorted.sortedBy { it.searchData.year ?: 0 }

                    // TODO: sort by watchdate instead of watchcount? if so, where does the "unwatched" go, beginning or end?
                    SortingCriteria.WatchCount ->
                        unsorted.sortedBy { it.appData.watchDates.size }

                    SortingCriteria.Genre ->
                        unsorted.sortedBy { it.searchData.genres.joinToString(",") }

                    SortingCriteria.Runtime ->
                        unsorted.sortedBy { it.detailData.runtimeMinutes }

                    SortingCriteria.Director ->
                        unsorted.sortedBy {
                            it.detailData.directorNames.joinToString(",")
                        }

                    SortingCriteria.MeanRating ->
                        unsorted.sortedBy { movie ->
                            listOf(
                                movie.detailData.rtRating.percentValue,
                                movie.detailData.mcRating.percentValue,
                            ).filter { it >= 0 }
                                .average()
                                .takeIf { !it.isNaN() } ?: 0.0
                        }

                    SortingCriteria.Random -> {
                        val order = List(unsorted.size) { randomizer.nextDouble() }
                        unsorted.zip(order).sortedBy { it.second }.map { it.first }
                    }
                }

            val sortedList =
                if (sortingSetup.direction == SortingDirection.Descending) {
                    sortedAscending.reversed()
                } else {
                    sortedAscending
                }
            return AsyncMovie.fromList(sortedList)
        }
    }
