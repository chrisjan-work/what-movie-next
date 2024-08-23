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
package com.lairofpixies.whatmovienext.views.state

data class ListFilters(
    val listMode: ListMode = ListMode.ALL,
    val year: MinMaxFilter = MinMaxFilter(null, null, false),
    val runtime: MinMaxFilter = MinMaxFilter(null, null, false),
    val rtScore: MinMaxFilter = MinMaxFilter(null, null, false),
    val mcScore: MinMaxFilter = MinMaxFilter(null, null, false),
    val genres: WordFilter = WordFilter(emptyList(), false),
    val directors: WordFilter = WordFilter(emptyList(), false),
)

data class MinMaxFilter(
    val min: Int?,
    val max: Int?,
    val isEnabled: Boolean,
) {
    val isNotEmpty = min != null || max != null
    val isActive: Boolean = isEnabled && isNotEmpty
}

data class WordFilter(
    val words: List<String>,
    val isEnabled: Boolean,
) {
    val isNotEmpty = words.isNotEmpty()
    val isActive: Boolean = isEnabled && isNotEmpty
}
