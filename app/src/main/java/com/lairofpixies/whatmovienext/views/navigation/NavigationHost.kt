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

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lairofpixies.whatmovienext.viewmodels.ArchiveViewModel
import com.lairofpixies.whatmovienext.viewmodels.MainViewModel
import com.lairofpixies.whatmovienext.viewmodels.MovieCardViewModel
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.viewmodels.ScreenViewModel
import com.lairofpixies.whatmovienext.viewmodels.SearchViewModel
import com.lairofpixies.whatmovienext.views.screens.archive.ArchiveScreen
import com.lairofpixies.whatmovienext.views.screens.card.MovieCardScreen
import com.lairofpixies.whatmovienext.views.screens.movielist.MovieListScreen
import com.lairofpixies.whatmovienext.views.screens.search.SearchScreen

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
            val createViewModel = hiltViewModel<SearchViewModel>().connect()
            SearchScreen(
                searchViewModel = createViewModel,
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
