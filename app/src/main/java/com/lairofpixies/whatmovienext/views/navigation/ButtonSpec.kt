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
package com.lairofpixies.whatmovienext.views.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.ui.graphics.vector.ImageVector
import com.lairofpixies.whatmovienext.R

sealed class ButtonSpec(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    // Shortcuts to other views
    data object MoviesShortcut : ButtonSpec(
        labelRes = R.string.movies,
        icon = Icons.Outlined.GridView,
    )

    data object CreateMovieShortcut : ButtonSpec(
        labelRes = R.string.add_new_movie,
        icon = Icons.Outlined.AddCircle,
    )

    data object EditShortcut : ButtonSpec(
        labelRes = R.string.edit,
        icon = Icons.Outlined.Edit,
    )

    data object SettingsAction : ButtonSpec(
        labelRes = R.string.settings,
        icon = Icons.Outlined.Settings,
    )

    data object ArchiveShortcut : ButtonSpec(
        labelRes = R.string.archive,
        icon = Icons.Outlined.FolderDelete,
    )

    data object ExportShortcut : ButtonSpec(
        labelRes = R.string.export_list,
        icon = Icons.Outlined.Upload,
    )

    data object ImportShortcut : ButtonSpec(
        labelRes = R.string.import_list,
        icon = Icons.Outlined.Download,
    )

    // Direct actions (save, archive, etc)

    data object SaveAction : ButtonSpec(
        labelRes = R.string.save,
        icon = Icons.Outlined.Check,
    )

    data object ShareAction : ButtonSpec(
        labelRes = R.string.share,
        icon = Icons.Outlined.Share,
    )

    data object ArchiveAction : ButtonSpec(
        labelRes = R.string.archive,
        icon = Icons.Outlined.Delete,
    )

    data object SearchAction : ButtonSpec(
        labelRes = R.string.lookup,
        icon = Icons.Outlined.TravelExplore,
    )

    data object ResultsAction : ButtonSpec(
        labelRes = R.string.results,
        icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
    )

    data object CancelAction : ButtonSpec(
        labelRes = R.string.cancel,
        icon = Icons.Outlined.Cancel,
    )

    data object RestoreAction : ButtonSpec(
        labelRes = R.string.restore,
        icon = Icons.Outlined.RestoreFromTrash,
    )

    data object DeleteAction : ButtonSpec(
        labelRes = R.string.delete_forever,
        icon = Icons.Outlined.Delete,
    )

    // State display: movie watch state
    data object PendingMovieState : ButtonSpec(
        labelRes = R.string.to_watch,
        icon = Icons.Outlined.WatchLater,
    )

    data object WatchedMovieState : ButtonSpec(
        labelRes = R.string.seen,
        icon = Icons.Outlined.RemoveRedEye,
    )

    // State display: movie list filter
    data object AllMoviesFilter : ButtonSpec(
        labelRes = R.string.all_movies,
        icon = Icons.Outlined.GridView,
    )

    data object PendingFilter : ButtonSpec(
        labelRes = R.string.to_watch,
        icon = Icons.Outlined.WatchLater,
    )

    data object WatchedFilter : ButtonSpec(
        labelRes = R.string.seen,
        icon = Icons.Outlined.RemoveRedEye,
    )

    data object SortingMenu : ButtonSpec(
        labelRes = R.string.sort,
        icon = Icons.Outlined.SwapVert,
    )

    data object FilterMenu : ButtonSpec(
        labelRes = R.string.filter,
        icon = Icons.AutoMirrored.Outlined.Rule,
    )

    data object ArrangeMenu : ButtonSpec(
        labelRes = R.string.arrange,
        icon = Icons.Outlined.SortByAlpha,
    )

    data object RouletteAction : ButtonSpec(
        labelRes = R.string.roulette,
        icon = Icons.Outlined.Casino,
    )

    data object QuickFindAction : ButtonSpec(
        labelRes = R.string.find,
        icon = Icons.Outlined.Search,
    )
}
