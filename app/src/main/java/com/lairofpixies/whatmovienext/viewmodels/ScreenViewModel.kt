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
import com.lairofpixies.whatmovienext.models.data.MovieData.NEW_ID
import com.lairofpixies.whatmovienext.models.data.hasMovie
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import kotlin.reflect.KClass

open class ScreenViewModel protected constructor() : ViewModel() {
    @Inject
    lateinit var randomizer: Random

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

    fun onNavigateTo(destination: Routes) {
        CoroutineScope(Dispatchers.Main).launch {
            navHostController.navigate(destination.route)
        }
    }

    fun onNavigateWithParam(
        destination: Routes,
        parameter: Long,
        popToHome: Boolean = false,
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            navHostController.navigate(destination.route(parameter)) {
                if (popToHome) {
                    popUpTo(Routes.HOME.route) {
                        inclusive = false
                    }
                }
            }
        }
    }

    fun showPopup(popupInfo: PopupInfo) = mainViewModel?.showPopup(popupInfo)

    fun closePopup() = mainViewModel?.closePopup()

    fun closePopupOfType(popupType: KClass<out PopupInfo>) = mainViewModel?.closePopupOfType(popupType)

    fun canSpinRoulette(): Boolean = mainViewModel?.listedMovies?.value?.hasMovie() == true

    fun onNavigateToRandomMovie(tabooId: Long = NEW_ID) {
        val mainViewModel = mainViewModel ?: return
        if (canSpinRoulette()) {
            val movieList =
                mainViewModel.listedMovies.value
                    .toList<Movie.ForList>()
                    .filter { it.appData.movieId != tabooId }
            val movieIndex = randomizer.nextInt(movieList.size)
            val movie = movieList.getOrNull(movieIndex) ?: return
            onNavigateWithParam(Routes.SingleMovieView, movie.appData.movieId, popToHome = true)
        }
    }
}
