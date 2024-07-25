package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class ScreenViewModel protected constructor() : ViewModel() {
    private lateinit var navHostController: NavHostController
    protected lateinit var mainViewModel: MainViewModel
        private set

    open fun attachNavHostController(navHostController: NavHostController) {
        this.navHostController = navHostController
    }

    open fun attachMainViewModel(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
    }

    fun onCancelAction() =
        CoroutineScope(Dispatchers.Main).launch {
            navHostController.navigate(Routes.HOME.route) {
                popUpTo(Routes.HOME.route) {
                    inclusive = true
                }
            }
        }

    fun onCloseWithIdAction(id: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            if (id == Movie.NEW_ID) {
                navHostController.popBackStack()
            } else {
                navHostController.navigate(Routes.SingleMovieView.route(id)) {
                    popUpTo(Routes.AllMoviesView.route) { inclusive = false }
                }
            }
        }
    }

    fun onNavigateTo(destination: Routes) {
        navHostController.navigate(destination.route)
    }

    fun onNavigateWithParam(
        destination: Routes,
        parameter: Long,
    ) {
        navHostController.navigate(destination.route(parameter))
    }

    fun showPopup(popupInfo: PopupInfo) = mainViewModel.showPopup(popupInfo)
}
