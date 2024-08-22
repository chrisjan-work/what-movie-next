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
        const val QUERY_EDITOR = "QueryEditor"
        const val SEARCH_RESULTS = "SearchResults"
        const val SELECTION_VIEW = "SelectionView"
        const val ARCHIVE = "Archive"
    }

    object Buttons {
        const val ARRANGE_MENU = "ArrangeMenuButton"
        const val SORT_TAB = "SortMenuTab"
        const val FILTER_TAB = "FilterMenuTab"
        const val LIST_MODE = "ListModeButton"
        const val RUNTIME_FILTER = "RuntimeFilterButton"
        const val ROULETTE = "RouletteButton"
        const val ARCHIVE_ACTION = "ArchiveAction"
        const val ARCHIVE_SHORTCUT = "ArchiveShortcut"
    }

    object Popups {
        const val EMPTY_TITLE = "EmptyTitle"
        const val CONFIRM_DELETION = "ConfirmDeletion"
        const val SEARCHING = "Searching"
        const val SEARCH_EMPTY = "SearchReturnedNothing"
        const val CONNECTION_FAILED = "OnlineConnectionError"
    }

    object Menus {
        const val SORTING = "SortingMenu"
    }

    object Items {
        const val MOVIE_LIST_ITEM = "MovieListItem"
    }
}
