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

import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
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
    fun `always new id`() {
        val tmdbMovieBasic = TmdbMovieBasic(tmdbId = 1, title = "Anything")
        val movie = remoteMapper.toMovie(tmdbMovieBasic)
        assertEquals(movie.id, Movie.NEW_ID)
    }

    @Test
    fun `copy verbatim data`() {
        val tmdbMovieBasic =
            TmdbMovieBasic(tmdbId = 1, title = "Anything", originalTitle = "Irgendetwas")
        val movie = remoteMapper.toMovie(tmdbMovieBasic)
        assertEquals(tmdbMovieBasic.tmdbId, movie.tmdbId)
        assertEquals(tmdbMovieBasic.title, movie.title)
        assertEquals(tmdbMovieBasic.originalTitle, movie.originalTitle)
    }

    @Test
    fun `parse year when date is valid`() {
        val tmdbMovieBasic =
            TmdbMovieBasic(tmdbId = 1, title = "Anything", releaseDate = "2001-01-01")
        val movie = remoteMapper.toMovie(tmdbMovieBasic)
        assertEquals(2001, movie.year)
    }

    @Test
    fun `parse year when date is invalid`() {
        val tmdbMovieBasic =
            TmdbMovieBasic(tmdbId = 1, title = "Anything", releaseDate = "2021")
        val movie = remoteMapper.toMovie(tmdbMovieBasic)
        assertEquals(null, movie.year)
    }

    @Test
    fun `parse year when date is missing`() {
        val tmdbMovieBasic =
            TmdbMovieBasic(tmdbId = 1, title = "Anything")
        val movie = remoteMapper.toMovie(tmdbMovieBasic)
        assertEquals(null, movie.year)
    }

    @Test
    fun `extract poster urls`() {
        // Given
        every { configRepo.getThumbnailUrl(any()) } answers {
            "localhost/thumb/${firstArg<String>()}"
        }
        every { configRepo.getCoverUrl(any()) } answers {
            "localhost/cover/${firstArg<String>()}"
        }
        val tmdbMovieBasic =
            TmdbMovieBasic(tmdbId = 1, title = "Anything", posterPath = "abcd")

        // When
        val movie = remoteMapper.toMovie(tmdbMovieBasic)

        // Then
        assertEquals("localhost/thumb/abcd", movie.thumbnailUrl)
        assertEquals("localhost/cover/abcd", movie.coverUrl)
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
    fun `map genres in movie`() {
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

        val tmdbMovieBasic =
            TmdbMovieBasic(tmdbId = 1, title = "Anything", genreIds = listOf(2))

        // When
        val movie = remoteMapper.toMovie(tmdbMovieBasic)

        // Then
        assertEquals(listOf("Comedy"), movie.genres)
    }

    @Test
    fun `extended tmdb movie to local movie`() {
        // Given
        every { configRepo.getCoverUrl("/terminator2.jpg") } returns "cover.jpg"
        every { configRepo.getThumbnailUrl("/terminator2.jpg") } returns "thumbnail.jpg"
        every { genreRepository.genreNamesByTmdbIds(listOf(188)) } returns listOf("Action")

        // When
        val result = remoteMapper.toMovie(testTmdbMovieExtended())

        // Then
        assertEquals(testLocalMovieExtended(), result)
    }
}
