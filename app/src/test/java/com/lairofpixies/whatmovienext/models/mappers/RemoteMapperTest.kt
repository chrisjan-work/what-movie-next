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

import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.RatingPair
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.data.OmdbMovieInfo
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo.Binding
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo.Bindings
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo.Results
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RemoteMapperTest {
    private lateinit var configRepo: ConfigRepository
    private lateinit var genreMapper: GenreMapper
    private lateinit var remoteMapper: RemoteMapper

    @Before
    fun setUp() {
        configRepo = mockk(relaxed = true)
        genreMapper = testGenreMapper()
        remoteMapper = RemoteMapper(configRepo, genreMapper)
    }

    @Test
    fun `parse year when date is valid`() {
        val releaseDate = "2001-01-01"
        val year = remoteMapper.toYear(releaseDate)
        assertEquals(2001, year)
    }

    @Test
    fun `parse year when date is invalid`() {
        val releaseDate = "2001"
        val year = remoteMapper.toYear(releaseDate)
        assertEquals(null, year)
    }

    @Test
    fun `parse year when date is missing`() {
        val releaseDate = null
        val year = remoteMapper.toYear(releaseDate)
        assertEquals(null, year)
    }

    @Test
    fun `parse percentage from string`() {
        // Given
        val percentageMatrix =
            listOf(
                "100%" to 100,
                "12.25%" to 12,
                "30%" to 30,
                "3/5" to 60,
                "71/100" to 71,
                "7" to 7,
                "0" to 0,
                "nope" to -1,
            )

        // When
        percentageMatrix.map { (percentage, expected) ->
            assertEquals(expected, remoteMapper.toPercent(percentage))
        }
    }

    @Test
    fun `parse ratings`() {
        // Given
        val omdbRatings = testOmdbMovieRatings()
        val wikiRatings = testWikidataMovieRatings()

        // When
        val result =
            remoteMapper.toRatings(
                omdbRatings = omdbRatings,
                wikidataMovieInfo = wikiRatings,
            )

        // Then
        val expected = testRatingMap()

        assertEquals(expected, result)
    }

    @Test
    fun `parse ratings - combine wikidata and omdb`() {
        // Given
        val metacriticOmdb =
            OmdbMovieInfo(
                success = "True",
                ratings =
                    listOf(
                        OmdbMovieInfo.OmdbRating(
                            source = "Metacritic",
                            value = "82/100",
                        ),
                    ),
            )

        val rottenTomatoesWikidata =
            WikidataMovieInfo(
                Results(
                    listOf(
                        Bindings(
                            entity = Binding("tt100", "literal"),
                            rottenTomatoesId = Binding("m/churminator_the_ii", "literal"),
                            rottenTomatoesRating = Binding("81%", "literal"),
                            metacriticId = Binding("", "literal"),
                            metacriticRating = Binding("", "literal"),
                        ),
                    ),
                ),
            )

        // When
        val result =
            remoteMapper.toRatings(
                omdbRatings = metacriticOmdb,
                wikidataMovieInfo = rottenTomatoesWikidata,
            )

        // Then
        val expected =
            testRatingMap().run {
                copy(mcRating = mcRating.copy(sourceId = ""))
            }

        assertEquals(expected, result)
    }

    @Test
    fun `parse ratings - wikidata fallback when omdb missing`() {
        // Given
        val failedOmdbRatings =
            OmdbMovieInfo(
                success = "False",
                errorMessage = "Whatever",
            )
        val wikiRatings = testWikidataMovieRatings()

        // When
        val result =
            remoteMapper.toRatings(
                omdbRatings = failedOmdbRatings,
                wikidataMovieInfo = wikiRatings,
            )

        // Then
        val expected = testRatingMap()

        assertEquals(expected, result)
    }

    @Test
    fun `parse ratings - wikidata missing but omdb present`() {
        // Given
        val failedOmdbRatings = testOmdbMovieRatings()
        val failedWiki =
            WikidataMovieInfo(
                results = Results(emptyList()),
            )

        // When
        val result =
            remoteMapper.toRatings(
                omdbRatings = failedOmdbRatings,
                wikidataMovieInfo = failedWiki,
            )

        // Then
        val expected =
            testRatingMap().run {
                copy(
                    rtRating = rtRating.copy(sourceId = ""),
                    mcRating = mcRating.copy(sourceId = ""),
                )
            }

        assertEquals(expected, result)
    }

    @Test
    fun `null or failing ratings`() {
        // Given
        val noOmdb: OmdbMovieInfo? = null
        val noWiki: WikidataMovieInfo? = null
        val failedOmdb =
            OmdbMovieInfo(
                success = "False",
                errorMessage = "Whatever",
            )
        val failedWiki =
            WikidataMovieInfo(
                results = Results(emptyList()),
            )

        // every combination always returns an "empty" rating pair
        val emptyRatings =
            RatingPair(
                rtRating =
                    Rating(
                        Rating.Rater.RottenTomatoes,
                        sourceId = "",
                        displayValue = "",
                        percentValue = -1,
                    ),
                mcRating =
                    Rating(
                        Rating.Rater.Metacritic,
                        sourceId = "",
                        displayValue = "",
                        percentValue = -1,
                    ),
            )
        assertEquals(emptyRatings, remoteMapper.toRatings(noOmdb, noWiki))
        assertEquals(emptyRatings, remoteMapper.toRatings(noOmdb, failedWiki))
        assertEquals(emptyRatings, remoteMapper.toRatings(failedOmdb, failedWiki))
        assertEquals(emptyRatings, remoteMapper.toRatings(failedOmdb, noWiki))
    }

    @Test
    fun `convert movie from search results`() {
        // Given
        every { configRepo.getThumbnailUrl(any()) } answers {
            "localhost/thumb/${firstArg<String>()}"
        }
        every { configRepo.getCoverUrl(any()) } answers {
            "localhost/cover/${firstArg<String>()}"
        }
        val tmdbMovieBasic =
            TmdbMovieBasic(
                tmdbId = 1,
                title = "Anything",
                originalTitle = "Irgendetwas",
                releaseDate = "2002-01-01",
                posterPath = "abcd",
                genreIds = listOf(35),
            )

        // When
        val searchData = remoteMapper.toSearchMovie(tmdbMovieBasic).searchData

        // Then
        assertEquals(tmdbMovieBasic.tmdbId, searchData.tmdbId)
        assertEquals(tmdbMovieBasic.title, searchData.title)
        assertEquals(tmdbMovieBasic.originalTitle, searchData.originalTitle)
        assertEquals(2002, searchData.year)
        assertEquals("localhost/thumb/abcd", searchData.thumbnailUrl)
        assertEquals("localhost/cover/abcd", searchData.coverUrl)
        assertEquals(listOf(35L), searchData.genreIds)
    }

    @Test
    fun `extended tmdb movie to card movie`() {
        // Given
        every { configRepo.getCoverUrl("/terminator2.jpg") } returns "cover.jpg"
        every { configRepo.getThumbnailUrl("/terminator2.jpg") } returns "thumbnail.jpg"
        every { configRepo.getFaceUrl(any()) } answers { firstArg() }

        // When
        val ratings = testRatingMap()
        val result = remoteMapper.toCardMovie(testTmdbMovieExtended(), ratings)

        // Then
        val expected = testCardMovieExtended().removeCreationTime()
        assertEquals(
            expected,
            result.removeCreationTime(),
        )
    }
}
