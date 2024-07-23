package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class ScreenViewModel protected constructor() : ViewModel() {
    private lateinit var navController: NavHostController
    protected lateinit var mainViewModel: MainViewModel
        private set

    fun attachNavController(navController: NavHostController) {
        this.navController = navController
    }

    fun attachMainViewModel(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
    }

    fun onCancelAction() =
        CoroutineScope(Dispatchers.Main).launch {
            navController?.navigate(Routes.HOME.route) {
                popUpTo(Routes.HOME.route) {
                    inclusive = true
                }
            }
        }

    fun onCloseWithIdAction(id: Long) {
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

    fun onNavigateToMovieList() {
        navController.navigate(Routes.AllMoviesView.route)
    }
}
