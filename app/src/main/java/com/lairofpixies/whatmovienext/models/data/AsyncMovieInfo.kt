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

sealed class AsyncMovieInfo {
    data object Loading : AsyncMovieInfo()

    data class Failed(
        val trowable: Throwable,
    ) : AsyncMovieInfo()

    data object Empty : AsyncMovieInfo()

    data class Single(
        val movie: Movie,
    ) : AsyncMovieInfo()

    data class Multiple(
        val movies: List<Movie>,
    ) : AsyncMovieInfo()

    companion object {
        fun fromList(movies: List<Movie>): AsyncMovieInfo =
            when (movies.size) {
                0 -> Empty
                1 -> Single(movies.first())
                else -> Multiple(movies)
            }
    }
}

fun AsyncMovieInfo?.isMissing(): Boolean =
    this == null ||
        this == AsyncMovieInfo.Empty ||
        this is AsyncMovieInfo.Failed

fun AsyncMovieInfo?.hasMovie(): Boolean =
    this != null &&
        (
            this is AsyncMovieInfo.Single ||
                this is AsyncMovieInfo.Multiple
        )

fun AsyncMovieInfo.toList(): List<Movie> =
    when (this) {
        is AsyncMovieInfo.Single -> listOf(movie)
        is AsyncMovieInfo.Multiple -> movies
        else -> emptyList()
    }

fun AsyncMovieInfo.filter(sieve: (Movie) -> Boolean): AsyncMovieInfo =
    when (this) {
        is AsyncMovieInfo.Single -> if (sieve(movie)) this else AsyncMovieInfo.Empty
        is AsyncMovieInfo.Multiple -> {
            AsyncMovieInfo.fromList(movies.filter(sieve))
        }

        else -> this
    }
