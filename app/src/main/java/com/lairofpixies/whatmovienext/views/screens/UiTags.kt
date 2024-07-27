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
package com.lairofpixies.whatmovienext.views.screens

object UiTags {
    object Screens {
        const val MOVIE_LIST = "MovieList"
        const val MOVIE_CARD = "MovieCard"
        const val EDIT_CARD = "EditCard"
        const val ARCHIVE = "Archive"
        const val SEARCH_RESULTS = "SearchResults"
    }

    object Buttons {
        const val LIST_MODE = "ListModeButton"
    }

    object Popups {
        const val EMPTY_TITLE = "EmptyTitle"
        const val UNSAVED_CHANGES = "UnsavedChanges"
        const val DUPLICATED_TITLE = "DuplicatedTitle"
        const val CONFIRM_DELETION = "ConfirmDeletion"
        const val SEARCHING = "Searching"
        const val SEARCH_EMPTY = "SearchReturnedNothing"
        const val SEARCH_FAILED = "OnlineConnectionError"
    }
}
