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

import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DbMapperTest {
    private lateinit var dbMapper: DbMapper

    @Before
    fun setUp() {
        dbMapper = DbMapper()
    }

    private fun generateDbMovie() =
        DbMovie(
            id = 22,
            tmdbId = 333,
            imdbId = "aaaa",
            title = "Something",
            originalTitle = "Etwas",
            year = 2020,
            thumbnailUrl = "thumbnailUrl",
            coverUrl = "coverUrl",
            tagline = "tagline",
            summary = "summary",
            genres = "Drama,Mystery",
            runtimeMinutes = 111,
            watchState = WatchState.WATCHED,
            isArchived = false,
        )

    private fun generateMovie() =
        Movie(
            id = 22,
            tmdbId = 333,
            imdbId = "aaaa",
            title = "Something",
            originalTitle = "Etwas",
            year = 2020,
            thumbnailUrl = "thumbnailUrl",
            coverUrl = "coverUrl",
            tagline = "tagline",
            summary = "summary",
            genres = listOf("Drama", "Mystery"),
            runtimeMinutes = 111,
            watchState = WatchState.WATCHED,
            isArchived = false,
        )

    @Test
    fun toMovie() {
        // Given
        val dbMovie = generateDbMovie()

        // When
        val movie = dbMapper.toMovie(dbMovie)

        // Then
        val expectedMovie = generateMovie()
        assertEquals(expectedMovie, movie)
    }

    @Test
    fun toMovies() {
        // Given
        val dbMovies = listOf(generateDbMovie())

        // When
        val movie = dbMapper.toMovies(dbMovies)

        // Then
        val expectedMovies = listOf(generateMovie())
        assertEquals(expectedMovies, movie)
    }

    @Test
    fun toAsyncMovies() {
        // Given
        val dbMovies = listOf(generateDbMovie())

        // When
        val movie = dbMapper.toAsyncMovies(dbMovies)

        // Then
        val expectedMovies = AsyncMovieInfo.Single(generateMovie())
        assertEquals(expectedMovies, movie)
    }

    @Test
    fun toDbMovie() {
        // Given
        val movie = generateMovie()

        // When
        val dbMovie = dbMapper.toDbMovie(movie)

        // Then
        val expectedDbMovie = generateDbMovie()
        assertEquals(expectedDbMovie, dbMovie)
    }

    @Test
    fun toGenres() {
        // When
        val result = dbMapper.toGenres("Character Study,Musical")
        // Then
        assertEquals(listOf("Character Study", "Musical"), result)
    }

    @Test
    fun toDbGenres() {
        // When
        val result = dbMapper.toDbGenres(listOf("Character Study", "Musical"))
        // Then
        assertEquals("Character Study,Musical", result)
    }
}
