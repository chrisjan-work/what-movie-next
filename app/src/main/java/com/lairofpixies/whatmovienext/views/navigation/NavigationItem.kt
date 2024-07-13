package com.lairofpixies.whatmovienext.views.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.ui.graphics.vector.ImageVector
import com.lairofpixies.whatmovienext.R

sealed class NavigationItem(
    @StringRes val label: Int,
    val icon: ImageVector,
    val route: String,
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
}
