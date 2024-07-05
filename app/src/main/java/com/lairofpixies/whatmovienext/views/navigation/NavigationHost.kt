package com.lairofpixies.whatmovienext.views.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel
import com.lairofpixies.whatmovienext.viewmodel.UiState
import com.lairofpixies.whatmovienext.views.screens.MovieDetailsScreen
import com.lairofpixies.whatmovienext.views.screens.MovieList

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    uiState: UiState,
    viewModel: MainViewModel,
) {
    NavHost(navController = navController, startDestination = Routes.HOME.route) {
        composable(Routes.AllMoviesView.route) {
            MovieList(
                uiState.listMode,
                uiState.movieList,
                onListModeChanged = { viewModel.setListMode(it) },
                onMovieClicked = { movie -> navController.navigate(Routes.SingleMovieView.route(movie.id)) },
            )
        }
        composable(
            route = Routes.SingleMovieView.route,
            arguments =
                listOf(
                    navArgument(Routes.SingleMovieView.argumentOrEmpty) {
                        type = NavType.IntType
                    },
                ),
        ) { entry ->
            MovieDetailsScreen(
                movieId = entry.arguments?.getInt(Routes.SingleMovieView.argumentOrEmpty),
                onCloseAction = { navController.popBackStack() },
                navController = navController,
                viewModel = viewModel,
            )
        }
    }
}
