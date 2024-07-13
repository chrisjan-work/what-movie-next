package com.lairofpixies.whatmovienext.views.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel
import com.lairofpixies.whatmovienext.viewmodel.UiState
import com.lairofpixies.whatmovienext.views.screens.EditableMovieDetailsScreen
import com.lairofpixies.whatmovienext.views.screens.MovieDetailsScreen
import com.lairofpixies.whatmovienext.views.screens.MovieList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    uiState: UiState,
    viewModel: MainViewModel,
) {
    val onCloseAction: () -> Unit =
        { CoroutineScope(Dispatchers.Main).launch { navController.popBackStack() } }

    val onCloseWithIdAction: (Long) -> Unit = { id ->
        CoroutineScope(Dispatchers.Main).launch {
            if (id == Movie.NEW_ID) {
                navController.popBackStack()
            } else {
                navController.navigate(Routes.SingleMovieView.route(id)) {
                    popUpTo(Routes.AllMoviesView.route) { inclusive = false }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME.route) {
        composable(Routes.AllMoviesView.route) {
            MovieList(
                uiState.listMode,
                uiState.movieList,
                onListModeChanged = { viewModel.setListMode(it) },
                onMovieClicked = { movie ->
                    navController.navigate(
                        Routes.SingleMovieView.route(
                            movie.id,
                        ),
                    )
                },
                navController = navController,
            )
        }
        composable(
            route = Routes.SingleMovieView.route,
            arguments =
                listOf(
                    navArgument(Routes.SingleMovieView.argumentOrEmpty) {
                        type = NavType.LongType
                    },
                ),
        ) { entry ->
            MovieDetailsScreen(
                movieId = entry.arguments?.getLong(Routes.SingleMovieView.argumentOrEmpty),
                onCloseAction = onCloseAction,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable(Routes.CreateMovieView.route) {
            EditableMovieDetailsScreen(
                movieId = null,
                onCloseWithIdAction = onCloseWithIdAction,
                viewModel = viewModel,
                navController = navController,
            )
        }
        composable(
            route = Routes.EditMovieView.route,
            arguments =
                listOf(
                    navArgument(Routes.EditMovieView.argumentOrEmpty) {
                        type = NavType.LongType
                    },
                ),
        ) { entry ->
            EditableMovieDetailsScreen(
                movieId = entry.arguments?.getLong(Routes.EditMovieView.argumentOrEmpty),
                onCloseWithIdAction = onCloseWithIdAction,
                viewModel = viewModel,
                navController = navController,
            )
        }
    }
}
