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

data class PagedMovies(
    val movies: AsyncMovie,
    // always load incrementally
    val lastPage: Int = 1,
    val pagesLeft: Int = 0,
) {
    companion object {
        val Loading = PagedMovies(AsyncMovie.Loading)

        val Empty = PagedMovies(AsyncMovie.Empty)

        @Suppress("ktlint:standard:function-naming")
        fun Failed(trowable: Throwable) = PagedMovies(AsyncMovie.Failed(trowable))

        fun fromList(movies: List<Movie>) = PagedMovies(AsyncMovie.fromList(movies))
    }
}
