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
