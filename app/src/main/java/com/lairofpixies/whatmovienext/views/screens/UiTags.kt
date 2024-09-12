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
        const val SHARED_VIEW = "SharedMovie"
        const val ARCHIVE = "Archive"
    }

    object Buttons {
        const val ARRANGE_MENU = "ArrangeMenuButton"
        const val SORT_TAB = "SortMenuTab"
        const val FILTER_TAB = "FilterMenuTab"
        const val LIST_MODE = "ListModeButton"
        const val RUNTIME_FILTER = "RuntimeFilterButton"
        const val YEAR_FILTER = "YearFilterButton"
        const val RT_SCORE_FILTER = "RtScoreFilter"
        const val MC_SCORE_FILTER = "McScoreFilter"
        const val GENRES_FILTER = "GenresFilter"
        const val DIRECTORS_FILTER = "DirectorsFilter"
        const val ROULETTE = "RouletteButton"
        const val EXTENDED_MENU_ICON = "ExtendedMenu"
        const val ARCHIVE_ACTION = "ArchiveAction"
        const val SHARE_ACTION = "ShareAction"
        const val ADD_NEW_MOVIE_ACTION = "AddNewMovie"
        const val QUICK_FIND_ACTION = "QuickFind"
        const val ARCHIVE_SHORTCUT = "ArchiveShortcut"
        const val EXPORT_SHORTCUT = "ExportShortcut"
        const val IMPORT_SHORTCUT = "ImportShortcut"
        const val SORT_BY_CREATION_TIME = "SortByCreationTime"
        const val SORT_BY_TITLE = "SortByTitle"
        const val SORT_BY_YEAR = "SortByYear"
        const val SORT_BY_SEEN = "SortBySeen"
        const val SORT_BY_GENRE = "SortByGenre"
        const val SORT_BY_RUNTIME = "SortByRuntime"
        const val SORT_BY_DIRECTOR = "SortByDirector"
        const val SORT_BY_SCORE = "SortByScore"
        const val SHUFFLE_ACTION = "ShuffleAction"
    }

    object Popups {
        const val EMPTY_TITLE = "EmptyTitle"
        const val CONFIRM_DELETION = "ConfirmDeletion"
        const val SEARCHING = "Searching"
        const val SEARCH_EMPTY = "SearchReturnedNothing"
        const val MOVIE_NOT_FOUND = "MovieNotFound"
        const val CONNECTION_FAILED = "OnlineConnectionError"
        const val NUMBER_SELECT = "NumberSelect"
        const val WORD_SELECT = "WordSelect"
    }

    object Menus {
        const val SORTING = "SortingMenu"
        const val FILTERING = "FilteringMenu"
        const val TOP_BAR = "TopBar"
    }

    object Items {
        const val MOVIE_LIST_ITEM = "MovieListItem"
    }
}
