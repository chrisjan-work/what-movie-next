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

import com.lairofpixies.whatmovienext.models.data.LoadingMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.WatchState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class DbMapperTest {
    private lateinit var dbMapper: DbMapper

    @Before
    fun setUp() {
        dbMapper = DbMapper()
    }

    // TODO: remove
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
    @Ignore("about to be removed")
    fun `db movie to movie`() {
        // Given
        val dbMovie = testDbMovieExtended()

        // When
        val movie = dbMapper.toMovie(dbMovie)

        // Then
        val expectedMovie = generateMovie()
        assertEquals(expectedMovie, movie)
    }

    @Test
    @Ignore("about to be removed")
    fun toMovies() {
        // Given
        val dbMovies = listOf(testDbMovieExtended())

        // When
        val movie = dbMapper.toMovies(dbMovies)

        // Then
        val expectedMovies = listOf(generateMovie())
        assertEquals(expectedMovies, movie)
    }

    @Test
    @Ignore("about to be removed")
    fun toLoadingMovies() {
        // Given
        val dbMovies = listOf(testDbMovieExtended())

        // When
        val movie = dbMapper.toLoadingMovies(dbMovies)

        // Then
        val expectedMovies = LoadingMovie.Single(generateMovie())
        assertEquals(expectedMovies, movie)
    }

    @Test
    @Ignore("about to delete soon, no need to fix")
    fun `movie to db movie`() {
        // Given
        val movie = generateMovie()

        // When
        val dbMovie = dbMapper.toDbMovie(movie)

        // Then
        val expectedDbMovie = testDbMovieExtended()
        assertEquals(expectedDbMovie, dbMovie)
    }

    @Test
    fun `db movie to card movie`() {
        // Given
        val dbMovie = testDbMovieExtended()

        // When
        val result = dbMapper.toCardMovie(dbMovie)

        // Then
        val expected = testCardMovieExtended()

        assertEquals(expected, result)
    }

    @Test
    fun `card movie to db movie`() {
        // Given
        val cardMovie = testCardMovieExtended()

        // When
        val result = dbMapper.toDbMovie(cardMovie)

        // Then
        val expected = testDbMovieExtended()
        assertEquals(expected, result)
    }

    @Test
    fun `db movie to list movie`() {
        // Given
        val dbMovie = testDbMovieExtended()

        // When
        val result = dbMapper.toListMovie(dbMovie)

        // Then
        val expected = testListMovieExtended()
        assertEquals(expected, result)
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
