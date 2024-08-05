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

sealed class LoadingAMovie {
    data object Loading : LoadingAMovie()

    data class Failed(
        val trowable: Throwable,
    ) : LoadingAMovie()

    data object Empty : LoadingAMovie()

    data class Single(
        val movie: AMovie,
    ) : LoadingAMovie()

    data class Multiple(
        val movies: List<AMovie>,
    ) : LoadingAMovie()

    companion object {
        fun <T : AMovie> fromList(movies: List<T>): LoadingAMovie =
            when (movies.size) {
                0 -> Empty
                1 -> Single(movies.first())
                else -> Multiple(movies)
            }
    }

    inline fun <reified T : AMovie> toList(): List<T> =
        when (this) {
            is Single -> (movie as? T)?.let { listOf(it) } ?: emptyList()
            is Multiple -> movies.filterIsInstance<T>()
            else -> emptyList()
        }

    fun filter(sieve: (AMovie) -> Boolean): LoadingAMovie =
        when (this) {
            is Single -> if (sieve(movie)) this else Empty
            is Multiple -> {
                fromList(movies.filter(sieve))
            }

            else -> this
        }
}

fun LoadingAMovie?.isMissing(): Boolean =
    this == null ||
        this == LoadingAMovie.Empty ||
        this is LoadingAMovie.Failed

fun LoadingAMovie?.hasMovie(): Boolean =
    this != null &&
        (
            this is LoadingAMovie.Single ||
                this is LoadingAMovie.Multiple
        )
