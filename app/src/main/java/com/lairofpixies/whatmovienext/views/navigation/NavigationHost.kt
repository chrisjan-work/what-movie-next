package com.lairofpixies.whatmovienext.views.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lairofpixies.whatmovienext.viewmodels.ArchiveViewModel
import com.lairofpixies.whatmovienext.viewmodels.EditCardViewModel
import com.lairofpixies.whatmovienext.viewmodels.MainViewModel
import com.lairofpixies.whatmovienext.viewmodels.MovieCardViewModel
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.viewmodels.ScreenViewModel
import com.lairofpixies.whatmovienext.views.screens.ArchiveScreen
import com.lairofpixies.whatmovienext.views.screens.EditCardScreen
import com.lairofpixies.whatmovienext.views.screens.MovieCardScreen
import com.lairofpixies.whatmovienext.views.screens.MovieListScreen

@Composable
fun NavigationHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    fun <T : ScreenViewModel> T.connect() =
        apply {
            attachNavHostController(navController)
            attachMainViewModel(mainViewModel)
        }

    NavHost(navController = navController, startDestination = Routes.HOME.route) {
        composable(Routes.AllMoviesView.route) {
            val movieListViewModel = hiltViewModel<MovieListViewModel>().connect()
            MovieListScreen(
                listViewModel = movieListViewModel,
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
            val cardViewModel = hiltViewModel<MovieCardViewModel>().connect()
            MovieCardScreen(
                movieId = entry.arguments?.getLong(Routes.SingleMovieView.argumentOrEmpty),
                cardViewModel = cardViewModel,
            )
        }
        composable(Routes.CreateMovieView.route) {
            val createViewModel = hiltViewModel<EditCardViewModel>().connect()
            EditCardScreen(
                movieId = null,
                editViewModel = createViewModel,
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
            val editViewModel = hiltViewModel<EditCardViewModel>().connect()
            EditCardScreen(
                movieId = entry.arguments?.getLong(Routes.EditMovieView.argumentOrEmpty),
                editViewModel = editViewModel,
            )
        }
        composable(Routes.ArchiveView.route) {
            val archiveViewModel = hiltViewModel<ArchiveViewModel>().connect()

            ArchiveScreen(
                archiveViewModel = archiveViewModel,
            )
        }
    }
}
