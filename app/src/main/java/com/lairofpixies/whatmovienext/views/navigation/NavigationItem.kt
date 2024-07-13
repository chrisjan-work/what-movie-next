package com.lairofpixies.whatmovienext.views.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.ui.graphics.vector.ImageVector
import com.lairofpixies.whatmovienext.R

sealed class NavigationItem(
    @StringRes val label: Int,
    val icon: ImageVector,
    val route: String? = null,
) {
    data object AllMovies : NavigationItem(
        label = R.string.movies,
        icon = Icons.Outlined.GridView,
        route = Routes.AllMoviesView.route,
    )

    data object CreateMovie : NavigationItem(
        label = R.string.add_new_movie,
        icon = Icons.Outlined.AddCircle,
        route = Routes.CreateMovieView.route,
    )

    data object SaveChanges : NavigationItem(
        label = R.string.save,
        icon = Icons.Outlined.Check,
    )

    data object Cancel : NavigationItem(
        label = R.string.cancel,
        icon = Icons.Outlined.Cancel,
    )

    data object Archive : NavigationItem(
        label = R.string.archive,
        icon = Icons.Outlined.Delete,
    )

    data object Edit : NavigationItem(
        label = R.string.edit,
        icon = Icons.Outlined.Edit,
    )
}
