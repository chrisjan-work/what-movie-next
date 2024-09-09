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
import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.TestMovie.forList
import com.lairofpixies.whatmovienext.models.mappers.testListMovieExtended
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import com.lairofpixies.whatmovienext.views.state.WordFilter
import com.lairofpixies.whatmovienext.views.state.WordIdFilter
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
    fun `default all pass through`() {
        // Given
        val movies =
            AsyncMovie.fromList(
                listOf(
                    testListMovieExtended(),
                    testListMovieExtended().run {
                        copy(
                            appData = appData.copy(movieId = 11, watchDates = listOf(111L)),
                            searchData =
                                searchData.copy(
                                    title = "Another",
                                    genreIds = listOf(123, 456),
                                    year = 1999,
                                ),
                        )
                    },
                    testListMovieExtended().run {
                        copy(
                            appData = appData.copy(movieId = 14),
                            searchData = searchData.copy(title = "Third", year = null),
                            detailData =
                                detailData.copy(
                                    runtimeMinutes = 181,
                                    rtRating =
                                        Rating(
                                            source = Rating.Rater.RottenTomatoes,
                                            sourceId = "1234",
                                            percentValue = 67,
                                            displayValue = "67%",
                                        ),
                                ),
                        )
                    },
                ),
            )

        // When
        val passThrough = filterProcessor.filterMovies(movies, ListFilters())

        // Then
        assertEquals(movies, passThrough)
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
                    byYear(
                        MinMaxFilter(
                            min = 1983,
                            max = null,
                            isEnabled = true,
                        ),
                    )
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byYear(
                        MinMaxFilter(
                            min = null,
                            max = 1988,
                            isEnabled = true,
                        ),
                    )
                }
            val both =
                inputMovies.toMutableList().apply {
                    byYear(
                        MinMaxFilter(
                            min = 1983,
                            max = 1988,
                            isEnabled = true,
                        ),
                    )
                }
            val none =
                inputMovies.toMutableList().apply {
                    byYear(
                        MinMaxFilter(
                            min = null,
                            max = null,
                            isEnabled = true,
                        ),
                    )
                }

            val disabled =
                inputMovies.toMutableList().apply {
                    byYear(
                        MinMaxFilter(
                            min = 1983,
                            max = 1988,
                            isEnabled = false,
                        ),
                    )
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
            assertEquals(inputMovies, disabled)
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
                    byNumber(
                        MinMaxFilter(
                            min = 150,
                            max = null,
                            isEnabled = true,
                        ),
                    ) { it.detailData.runtimeMinutes }
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = null,
                            max = 250,
                            isEnabled = true,
                        ),
                    ) { it.detailData.runtimeMinutes }
                }
            val both =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 150,
                            max = 250,
                            isEnabled = true,
                        ),
                    ) { it.detailData.runtimeMinutes }
                }
            val none =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = null,
                            max = null,
                            isEnabled = true,
                        ),
                    ) { it.detailData.runtimeMinutes }
                }
            val disabled =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 150,
                            max = 250,
                            isEnabled = false,
                        ),
                    ) { it.detailData.runtimeMinutes }
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
            assertEquals(inputMovies, disabled)
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
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = null,
                            isEnabled = true,
                        ),
                    ) { it.detailData.rtRating.percentValue }
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = null,
                            max = 60,
                            isEnabled = true,
                        ),
                    ) { it.detailData.rtRating.percentValue }
                }
            val both =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = 60,
                            isEnabled = true,
                        ),
                    ) { it.detailData.rtRating.percentValue }
                }
            val none =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = null,
                            max = null,
                            isEnabled = true,
                        ),
                    ) { it.detailData.rtRating.percentValue }
                }
            val disabled =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = 60,
                            isEnabled = false,
                        ),
                    ) { it.detailData.rtRating.percentValue }
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
            assertEquals(inputMovies, disabled)
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
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = null,
                            isEnabled = true,
                        ),
                    ) { it.detailData.mcRating.percentValue }
                }
            val onlyMax =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = null,
                            max = 60,
                            isEnabled = true,
                        ),
                    ) { it.detailData.mcRating.percentValue }
                }
            val both =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = 60,
                            isEnabled = true,
                        ),
                    ) { it.detailData.mcRating.percentValue }
                }
            val none =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = null,
                            max = null,
                            isEnabled = true,
                        ),
                    ) { it.detailData.mcRating.percentValue }
                }
            val disabled =
                inputMovies.toMutableList().apply {
                    byNumber(
                        MinMaxFilter(
                            min = 30,
                            max = 60,
                            isEnabled = false,
                        ),
                    ) { it.detailData.mcRating.percentValue }
                }

            // Then
            assertEquals(inputMovies.takeLast(2), onlyMin)
            assertEquals(inputMovies.take(2), onlyMax)
            assertEquals(listOf(inputMovies[1]), both)
            assertEquals(inputMovies, none)
            assertEquals(inputMovies, disabled)
        }

    @Test
    fun `filter by genre name`() {
        // Given
        val inputMovies =
            listOf(
                forList(title = "one", genreNames = listOf("Action")),
                forList(title = "two", genreNames = listOf("Action", "Drama")),
                forList(title = "three", genreNames = listOf("Mystery")),
            )

        // When
        fun filterBy(vararg genres: String) =
            inputMovies.toMutableList().apply {
                with(filterProcessor) {
                    byText(
                        WordFilter(
                            genres.toList(),
                            true,
                        ),
                    ) { it.searchData.genreNames }
                }
            }

        val action = filterBy("Action")
        val actionOrDrama = filterBy("Action", "Drama")
        val dramaOrMystery = filterBy("Mystery", "Drama")
        val anything = filterBy()
        val disabled =
            inputMovies.toMutableList().apply {
                with(filterProcessor) {
                    byText(
                        WordFilter(
                            listOf("Action"),
                            false,
                        ),
                    ) { it.searchData.genreNames }
                }
            }

        // Then
        assertEquals(inputMovies.take(2), action)
        assertEquals(inputMovies.take(2), actionOrDrama)
        assertEquals(inputMovies.takeLast(2), dramaOrMystery)
        assertEquals(inputMovies, anything)
        assertEquals(inputMovies, disabled)
    }

    @Test
    fun `filter by genre id`() {
        // Given
        val inputMovies =
            listOf(
                forList(title = "one", genreIds = listOf(28)),
                forList(title = "two", genreIds = listOf(28, 18)),
                forList(title = "three", genreIds = listOf(9648)),
            )

        // When
        fun filterBy(vararg genres: Long) =
            inputMovies.toMutableList().apply {
                with(filterProcessor) {
                    byGenreId(
                        WordIdFilter(
                            genres.toList(),
                            true,
                        ),
                    ) { it.searchData.genreIds }
                }
            }

        val action = filterBy(28)
        val actionOrDrama = filterBy(28, 18)
        val dramaOrMystery = filterBy(9648, 18)
        val anything = filterBy()
        val disabled =
            inputMovies.toMutableList().apply {
                with(filterProcessor) {
                    byGenreId(
                        WordIdFilter(
                            listOf(28),
                            false,
                        ),
                    ) { it.searchData.genreIds }
                }
            }

        // Then
        assertEquals(inputMovies.take(2), action)
        assertEquals(inputMovies.take(2), actionOrDrama)
        assertEquals(inputMovies.takeLast(2), dramaOrMystery)
        assertEquals(inputMovies, anything)
        assertEquals(inputMovies, disabled)
    }

    @Test
    fun `filter by director`() {
        // Given
        val inputMovies =
            listOf(
                forList(title = "one", directors = listOf("Max")),
                forList(title = "two", directors = listOf("Sam")),
            )

        // When
        fun filterBy(vararg directors: String) =
            inputMovies.toMutableList().apply {
                with(filterProcessor) {
                    byText(
                        WordFilter(
                            directors.toList(),
                            true,
                        ),
                    ) { it.detailData.directorNames }
                }
            }

        val first = filterBy("Max")
        val second = filterBy("Sam")
        val both = filterBy("Sam", "Max")
        val anything = filterBy()
        val disabled =
            inputMovies.toMutableList().apply {
                with(filterProcessor) {
                    byText(
                        WordFilter(
                            listOf("Sam", "Max"),
                            false,
                        ),
                    ) { it.detailData.directorNames }
                }
            }

        // Then
        assertEquals(inputMovies.take(1), first)
        assertEquals(inputMovies.takeLast(1), second)
        assertEquals(inputMovies, both)
        assertEquals(inputMovies, anything)
        assertEquals(inputMovies, disabled)
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
                directors = listOf("Joe"),
                genreIds = listOf(35),
                genreNames = listOf("Comedy"),
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
                    survivor.copy(
                        detailData =
                            survivor.detailData.copy(
                                directorNames =
                                    listOf(
                                        "Jack",
                                    ),
                            ),
                    ),
                    survivor.copy(
                        searchData =
                            survivor.searchData.copy(
                                genreIds = listOf(27),
                                genreNames = listOf("Horror"),
                            ),
                    ),
                ),
            )
        val listFilters =
            ListFilters(
                listMode = ListMode.ALL,
                year = MinMaxFilter(1990, 2001, true),
                runtime = MinMaxFilter(100, 120, true),
                rtScore = MinMaxFilter(50, 80, true),
                mcScore = MinMaxFilter(50, 80, true),
                genres = WordIdFilter(listOf(35), true),
                directors = WordFilter(listOf("Joe"), true),
            )
        // When
        val result = filterProcessor.filterMovies(movieList, listFilters)

        // Then
        assertEquals(AsyncMovie.Single(survivor), result)
    }
}
