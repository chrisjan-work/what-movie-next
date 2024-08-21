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
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FilterProcessorTest {
    private lateinit var filterProcessor: FilterProcessor

    @Before
    fun setUp() {
        filterProcessor = FilterProcessor()
    }

    @Test
    fun `update movie list with all movies filter`() {
        // Given
        val seenMovie =
            forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
        val unseenMovie =
            forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
        val originalList = listOf(seenMovie, unseenMovie)

        // When
        val result = originalList.toMutableList()
        with(filterProcessor) { result.byListMode(ListMode.ALL) }

        // Then
        assertEquals(originalList, result)
    }

    @Test
    fun `update movie list with only unseen movies`() {
        // Given
        val seenMovie =
            forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
        val unseenMovie =
            forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
        val originalList = listOf(seenMovie, unseenMovie)
        // When
        val result = originalList.toMutableList()
        with(filterProcessor) { result.byListMode(ListMode.PENDING) }

        // Then
        assertEquals(listOf(unseenMovie), result)
    }

    @Test
    fun `update movie list with only seen movies`() {
        // Given
        val seenMovie =
            forList(id = 23, title = "The Number 23", watchDates = listOf(667788L))
        val unseenMovie =
            forList(id = 9, title = "Plan 9 from Outer Space", watchDates = emptyList())
        val originalList = listOf(seenMovie, unseenMovie)

        // When
        val result = originalList.toMutableList()
        with(filterProcessor) { result.byListMode(ListMode.WATCHED) }

        // Then
        assertEquals(listOf(seenMovie), result)
    }

    @Test
    fun `filter by year`() =
        with(filterProcessor) {
            // Given
            val inputMovies =
                listOf(
                    forList(title = "one", year = 1980),
                    forList(title = "two", year = 1985),
                    forList(title = "three", year = 1990),
                )

            // When
            val onlyMin =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(min = 1983)) { it?.searchData?.year }
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(max = 1988)) { it?.searchData?.year }
                }
            val both =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(min = 1983, max = 1988)) { it?.searchData?.year }
                }
            val none =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter()) { it?.searchData?.year }
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
        }

    @Test
    fun `filter by runtime`() =
        with(filterProcessor) {
            // Given
            val inputMovies =
                listOf(
                    forList(title = "one", runtimeMinutes = 100),
                    forList(title = "two", runtimeMinutes = 200),
                    forList(title = "three", runtimeMinutes = 300),
                )

            // When
            val onlyMin =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(min = 150)) { it?.detailData?.runtimeMinutes }
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(max = 250)) { it?.detailData?.runtimeMinutes }
                }
            val both =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(min = 150, max = 250)) { it?.detailData?.runtimeMinutes }
                }
            val none =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter()) { it?.detailData?.runtimeMinutes }
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
        }

    @Test
    fun `filter by rotten tomatoes score`() =
        with(filterProcessor) {
            // Given
            val inputMovies =
                listOf(
                    forList(title = "one", rtRating = 25),
                    forList(title = "two", rtRating = 50),
                    forList(title = "three", rtRating = 75),
                )

            // When
            val onlyMin =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(min = 30)) { it?.detailData?.rtRating?.percentValue }
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(max = 60)) { it?.detailData?.rtRating?.percentValue }
                }
            val both =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = 60,
                        ),
                    ) { it?.detailData?.rtRating?.percentValue }
                }
            val none =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter()) { it?.detailData?.rtRating?.percentValue }
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
        }

    @Test
    fun `filter by metacritic score`() =
        with(filterProcessor) {
            // Given
            val inputMovies =
                listOf(
                    forList(title = "one", mcRating = 25),
                    forList(title = "two", mcRating = 50),
                    forList(title = "three", mcRating = 75),
                )

            // When
            val onlyMin =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(min = 30)) { it?.detailData?.mcRating?.percentValue }
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter(max = 60)) { it?.detailData?.mcRating?.percentValue }
                }
            val both =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = 60,
                        ),
                    ) { it?.detailData?.mcRating?.percentValue }
                }
            val none =
                inputMovies.toMutableList().apply {
                    byNumber(MinMaxFilter()) { it?.detailData?.mcRating?.percentValue }
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
        }

    @Test
    fun `all filters on`() {
        val survivor =
            forList(
                id = 1,
                title = "Survivor",
                year = 1996,
                runtimeMinutes = 115,
                rtRating = 70,
                mcRating = 70,
            )
        val movieList =
            AsyncMovie.Multiple(
                listOf(
                    survivor,
                    survivor.copy(searchData = survivor.searchData.copy(year = 1980)),
                    survivor.copy(searchData = survivor.searchData.copy(year = 2011)),
                    survivor.copy(detailData = survivor.detailData.copy(runtimeMinutes = 90)),
                    survivor.copy(detailData = survivor.detailData.copy(runtimeMinutes = 140)),
                    survivor.copy(
                        detailData =
                            survivor.detailData.copy(
                                rtRating =
                                    survivor.detailData.rtRating.copy(
                                        percentValue = 10,
                                    ),
                            ),
                    ),
                    survivor.copy(
                        detailData =
                            survivor.detailData.copy(
                                rtRating =
                                    survivor.detailData.rtRating.copy(
                                        percentValue = 90,
                                    ),
                            ),
                    ),
                    survivor.copy(
                        detailData =
                            survivor.detailData.copy(
                                mcRating =
                                    survivor.detailData.mcRating.copy(
                                        percentValue = 10,
                                    ),
                            ),
                    ),
                    survivor.copy(
                        detailData =
                            survivor.detailData.copy(
                                mcRating =
                                    survivor.detailData.mcRating.copy(
                                        percentValue = 90,
                                    ),
                            ),
                    ),
                ),
            )
        val listFilters =
            ListFilters(
                listMode = ListMode.ALL,
                year = MinMaxFilter(1990, 2001),
                runtime = MinMaxFilter(100, 120),
                rtScore = MinMaxFilter(50, 80),
                mcScore = MinMaxFilter(50, 80),
            )
        // When
        val result = filterProcessor.filterMovies(movieList, listFilters)

        // Then
        assertEquals(AsyncMovie.Single(survivor), result)
    }
}
