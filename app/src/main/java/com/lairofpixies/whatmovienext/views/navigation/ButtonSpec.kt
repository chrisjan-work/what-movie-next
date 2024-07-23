package com.lairofpixies.whatmovienext.views.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.TravelExplore
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

    data object ArchiveShortcut : ButtonSpec(
        labelRes = R.string.archive,
        icon = Icons.Outlined.FolderDelete,
    )

    // Direct actions (save, archive, etc)

    data object SaveAction : ButtonSpec(
        labelRes = R.string.save,
        icon = Icons.Outlined.Check,
    )

    data object ArchiveAction : ButtonSpec(
        labelRes = R.string.archive,
        icon = Icons.Outlined.Delete,
    )

    data object SearchAction : ButtonSpec(
        labelRes = R.string.lookup,
        icon = Icons.Outlined.TravelExplore,
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
}
