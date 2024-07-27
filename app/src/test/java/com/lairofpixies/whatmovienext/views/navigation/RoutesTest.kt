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
package com.lairofpixies.whatmovienext.views.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {
    @Test
    fun `route for movie list`() {
        assertEquals("movies", Routes.AllMoviesView.route)
    }

    @Test
    fun `route for movie details`() {
        assertEquals("movie_details/{movieId}", Routes.SingleMovieView.route)
    }

    @Test
    fun `route for movie details with argument`() {
        assertEquals("movie_details/123", Routes.SingleMovieView.route(123))
    }

    @Test
    fun `argument for movie details`() {
        assertEquals("movieId", Routes.SingleMovieView.argumentOrEmpty)
    }

    @Test
    fun `route for movie create`() {
        assertEquals("movie_create", Routes.CreateMovieView.route)
    }

    @Test
    fun `route for movie edit`() {
        assertEquals("movie_edit/{movieId}", Routes.EditMovieView.route)
    }

    @Test
    fun `route for movie edit with argument`() {
        assertEquals("movie_edit/123", Routes.EditMovieView.route(123))
    }

    @Test
    fun `argument for movie edit`() {
        assertEquals("movieId", Routes.EditMovieView.argumentOrEmpty)
    }

    @Test
    fun `route for archive`() {
        assertEquals("archive", Routes.ArchiveView.route)
    }

    @Test
    fun `route for home`() {
        assertEquals(Routes.AllMoviesView.route, Routes.HOME.route)
    }
}
