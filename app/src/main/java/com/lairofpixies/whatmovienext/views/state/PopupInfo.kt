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

sealed class PopupInfo {
    data object None : PopupInfo()

    data object EmptyTitle : PopupInfo()

    data class ConfirmDeletion(
        val onConfirm: () -> Unit,
    ) : PopupInfo()

    data class Searching(
        val onCancel: () -> Unit,
    ) : PopupInfo()

    data object SearchEmpty : PopupInfo()

    data object ConnectionFailed : PopupInfo()

    data class NumberChooser(
        val label: String,
        val filterValues: MinMaxFilter,
        val valueToText: (Int?) -> String,
        val textToValue: (String) -> Int?,
        val onConfirm: (MinMaxFilter) -> Unit,
    ) : PopupInfo()

    data class WordChooser(
        val label: String,
        val filterValues: WordFilter,
        val candidates: List<String>,
        val onConfirm: (WordFilter) -> Unit,
    ) : PopupInfo()
}
