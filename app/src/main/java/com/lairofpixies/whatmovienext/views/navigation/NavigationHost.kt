package com.lairofpixies.whatmovienext.views.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.viewmodels.ArchiveViewModel
import com.lairofpixies.whatmovienext.viewmodels.MainViewModel
import com.lairofpixies.whatmovienext.views.screens.ArchiveScreen
import com.lairofpixies.whatmovienext.views.screens.EditCardScreen
import com.lairofpixies.whatmovienext.views.screens.MovieCardScreen
import com.lairofpixies.whatmovienext.views.screens.MovieListScreen
import com.lairofpixies.whatmovienext.views.state.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    uiState: UiState,
    mainViewModel: MainViewModel,
) {
    val onCancelAction: () -> Unit = {
        navController.navigate(Routes.HOME.route) {
            popUpTo(Routes.HOME.route) {
                inclusive = true
            }
        }
    }

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
            MovieListScreen(
                listMode = uiState.listMode,
                movies = uiState.movieList,
                isArchiveVisitable = uiState.archiveList.isNotEmpty(),
                onListModeChanged = { mainViewModel.setListMode(it) },
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
            MovieCardScreen(
                movieId = entry.arguments?.getLong(Routes.SingleMovieView.argumentOrEmpty),
                onCancelAction = onCancelAction,
                navController = navController,
                viewModel = mainViewModel,
            )
        }
        composable(Routes.CreateMovieView.route) {
            EditCardScreen(
                movieId = null,
                onCloseWithIdAction = onCloseWithIdAction,
                onCancelAction = onCancelAction,
                viewModel = mainViewModel,
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
            EditCardScreen(
                movieId = entry.arguments?.getLong(Routes.EditMovieView.argumentOrEmpty),
                onCloseWithIdAction = onCloseWithIdAction,
                onCancelAction = onCancelAction,
                viewModel = mainViewModel,
                navController = navController,
            )
        }
        composable(Routes.ArchiveView.route) {
            val archiveViewModel: ArchiveViewModel =
                hiltViewModel<ArchiveViewModel>().apply {
                    attachNavController(navController)
                    attachMainViewModel(mainViewModel)
                }

            ArchiveScreen(
                archiveViewModel = archiveViewModel,
            )
        }
    }
}
