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
}
