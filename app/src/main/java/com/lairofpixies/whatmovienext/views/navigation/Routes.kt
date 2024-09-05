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

enum class Routes(
    private val path: String,
    private val argument: String? = null,
) {
    AllMoviesView(path = "movies"),
    SingleMovieView(path = "movie_details", argument = "movieId"),
    CreateMovieView(path = "movie_create"),
    SharedMovieView(path = "movie_shared", argument = "tmdbId"),
    ArchiveView(path = "archive"),
    ;

    val route
        get() = path + (argument?.let { "/{$it}" } ?: "")

    fun route(param: Long) = path + (argument?.let { "/$param" } ?: "")

    val argumentOrEmpty = argument ?: ""

    companion object {
        val HOME = AllMoviesView
    }
}
