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
package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

open class ScreenViewModel protected constructor() : ViewModel() {
    private lateinit var navHostController: NavHostController
    protected var mainViewModel: MainViewModel? = null
        private set

    open fun attachNavHostController(navHostController: NavHostController) {
        this.navHostController = navHostController
    }

    open fun attachMainViewModel(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
    }

    fun onLeaveAction() =
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

    fun showPopup(popupInfo: PopupInfo) = mainViewModel?.showPopup(popupInfo)

    fun closePopup() = mainViewModel?.closePopup()

    fun closePopupOfType(popupType: KClass<out PopupInfo>) = mainViewModel?.closePopupOfType(popupType)
}
