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

sealed class Movie {
    open val appData: MovieData.AppData? = null
    open val searchData: MovieData.SearchData? = null
    open val detailData: MovieData.DetailData? = null
    open val staffData: MovieData.StaffData? = null

    data class ForSearch(
        override val searchData: MovieData.SearchData,
    ) : Movie()

    data class ForList(
        override val appData: MovieData.AppData,
        override val searchData: MovieData.SearchData,
        override val detailData: MovieData.DetailData,
    ) : Movie()

    data class ForCard(
        override val appData: MovieData.AppData,
        override val searchData: MovieData.SearchData,
        override val detailData: MovieData.DetailData,
        override val staffData: MovieData.StaffData,
    ) : Movie()
}
