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
import com.lairofpixies.whatmovienext.models.data.RatingMap
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.data.OmdbMovieInfo
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RemoteMapperTest {
    private lateinit var configRepo: ConfigRepository
    private lateinit var genreRepository: GenreRepository
    private lateinit var remoteMapper: RemoteMapper

    @Before
    fun setUp() {
        configRepo = mockk(relaxed = true)
        genreRepository = mockk(relaxed = true)
        remoteMapper = RemoteMapper(configRepo, genreRepository)
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
    fun `map genres from api to db`() {
        // Given
        val tmdbGenres =
            TmdbGenres(
                genres =
                    listOf(
                        TmdbGenres.TmdbGenre(tmdbId = 1, name = "Action"),
                        TmdbGenres.TmdbGenre(tmdbId = 2, name = "Comedy"),
                    ),
            )
        // When
        val result = remoteMapper.toDbGenres(tmdbGenres)

        // Then
        assertEquals(
            listOf(
                DbGenre("Action", 1),
                DbGenre("Comedy", 2),
            ),
            result,
        )
    }

    @Test
    fun `map genre names out of ids stored in db`() {
        // Given
        val knownGenres =
            mapOf(
                1L to "Action",
                2L to "Comedy",
            )
        every { genreRepository.genreNamesByTmdbIds(any()) } answers {
            val queryIds = firstArg<List<Long>>()
            queryIds.mapNotNull { knownGenres[it] }
        }

        // When
        val result = remoteMapper.toGenreNames(listOf(2, 1, 2))

        // Then
        assertEquals(listOf("Comedy", "Action", "Comedy"), result)
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
                "nope" to 0,
            )

        // When
        percentageMatrix.map { (percentage, expected) ->
            assertEquals(expected, remoteMapper.toPercent(percentage))
        }
    }

    @Test
    fun `parse ratings`() {
        // Given
        val ratings = testOmdbMovieRatings()

        // When
        val result = remoteMapper.toRatings(ratings)

        // Then
        val expected = testRatingMap()

        assertEquals(expected, result)
    }

    @Test
    fun `null or failing ratings`() {
        // Given
        val noInfo: OmdbMovieInfo? = null
        val failedInfo =
            OmdbMovieInfo(
                success = "False",
                errorMessage = "Whatever",
            )

        val noRatings: RatingMap = emptyMap()
        assertEquals(noRatings, remoteMapper.toRatings(noInfo))
        assertEquals(noRatings, remoteMapper.toRatings(failedInfo))
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
        val knownGenres =
            mapOf(
                1L to "Action",
                2L to "Comedy",
            )
        every { genreRepository.genreNamesByTmdbIds(any()) } answers {
            val queryIds = firstArg<List<Long>>()
            queryIds.mapNotNull { knownGenres[it] }
        }

        val tmdbMovieBasic =
            TmdbMovieBasic(
                tmdbId = 1,
                title = "Anything",
                originalTitle = "Irgendetwas",
                releaseDate = "2002-01-01",
                posterPath = "abcd",
                genreIds = listOf(2),
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
        assertEquals(listOf("Comedy"), searchData.genres)
    }

    @Test
    fun `extended tmdb movie to card movie`() {
        // Given
        every { configRepo.getCoverUrl("/terminator2.jpg") } returns "cover.jpg"
        every { configRepo.getThumbnailUrl("/terminator2.jpg") } returns "thumbnail.jpg"
        every { configRepo.getFaceUrl(any()) } answers { firstArg() }
        every { genreRepository.genreNamesByTmdbIds(listOf(188)) } returns listOf("Action")

        // When
        val ratings = testRatingMap()
        val result = remoteMapper.toCardMovie(testTmdbMovieExtended(), ratings)

        // Then
        // TODO: remove this when ratings are part of db
        val expected =
            testCardMovieExtended().removeCreationTime().run {
                copy(
                    detailData =
                        detailData.copy(
                            rtRating = ratings[Rating.Rater.RottenTomatoes],
                            mcRating = ratings[Rating.Rater.Metacritic],
                        ),
                )
            }
        assertEquals(
            expected,
            result.removeCreationTime(),
        )
    }
}
