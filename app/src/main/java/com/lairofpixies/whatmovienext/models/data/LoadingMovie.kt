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
package com.lairofpixies.whatmovienext.models.data

sealed class LoadingMovie {
    data object Loading : LoadingMovie()

    data class Failed(
        val trowable: Throwable,
    ) : LoadingMovie()

    data object Empty : LoadingMovie()

    data class Single(
        val movie: Movie,
    ) : LoadingMovie()

    data class Multiple(
        val movies: List<Movie>,
    ) : LoadingMovie()

    companion object {
        fun fromList(movies: List<Movie>): LoadingMovie =
            when (movies.size) {
                0 -> Empty
                1 -> Single(movies.first())
                else -> Multiple(movies)
            }
    }

    fun toList(): List<Movie> =
        when (this) {
            is LoadingMovie.Single -> listOf(movie)
            is LoadingMovie.Multiple -> movies
            else -> emptyList()
        }

    fun filter(sieve: (Movie) -> Boolean): LoadingMovie =
        when (this) {
            is LoadingMovie.Single -> if (sieve(movie)) this else LoadingMovie.Empty
            is LoadingMovie.Multiple -> {
                LoadingMovie.fromList(movies.filter(sieve))
            }

            else -> this
        }
}

fun LoadingMovie?.isMissing(): Boolean =
    this == null ||
        this == LoadingMovie.Empty ||
        this is LoadingMovie.Failed

fun LoadingMovie?.hasMovie(): Boolean =
    this != null &&
        (
            this is LoadingMovie.Single ||
                this is LoadingMovie.Multiple
        )
