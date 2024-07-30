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
import com.lairofpixies.whatmovienext.models.data.remote.RemoteMovieSummary
import com.lairofpixies.whatmovienext.models.network.BackendConfigRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RemoteMapperTest {
    private lateinit var configRepo: BackendConfigRepository
    private lateinit var remoteMapper: RemoteMapper

    @Before
    fun setUp() {
        configRepo = mockk(relaxed = true)
        remoteMapper = RemoteMapper(configRepo)
    }

    @Test
    fun `always new id`() {
        val remoteMovieSummary = RemoteMovieSummary(tmdbId = 1, title = "Anything")
        val movie = remoteMapper.toMovie(remoteMovieSummary)
        assertEquals(movie.id, Movie.NEW_ID)
    }

    @Test
    fun `copy verbatim data`() {
        val remoteMovieSummary =
            RemoteMovieSummary(tmdbId = 1, title = "Anything", originalTitle = "Irgendetwas")
        val movie = remoteMapper.toMovie(remoteMovieSummary)
        assertEquals(remoteMovieSummary.tmdbId, movie.tmdbId)
        assertEquals(remoteMovieSummary.title, movie.title)
        assertEquals(remoteMovieSummary.originalTitle, movie.originalTitle)
    }

    @Test
    fun `parse year when date is valid`() {
        val remoteMovieSummary =
            RemoteMovieSummary(tmdbId = 1, title = "Anything", releaseDate = "2001-01-01")
        val movie = remoteMapper.toMovie(remoteMovieSummary)
        assertEquals(2001, movie.year)
    }

    @Test
    fun `parse year when date is invalid`() {
        val remoteMovieSummary =
            RemoteMovieSummary(tmdbId = 1, title = "Anything", releaseDate = "2021")
        val movie = remoteMapper.toMovie(remoteMovieSummary)
        assertEquals(null, movie.year)
    }

    @Test
    fun `parse year when date is missing`() {
        val remoteMovieSummary =
            RemoteMovieSummary(tmdbId = 1, title = "Anything")
        val movie = remoteMapper.toMovie(remoteMovieSummary)
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
        val remoteMovieSummary =
            RemoteMovieSummary(tmdbId = 1, title = "Anything", posterPath = "abcd")

        // When
        val movie = remoteMapper.toMovie(remoteMovieSummary)

        // Then
        assertEquals("localhost/thumb/abcd", movie.thumbnailUrl)
        assertEquals("localhost/cover/abcd", movie.coverUrl)
    }
}
