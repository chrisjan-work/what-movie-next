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

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.BuildConfig
import com.lairofpixies.whatmovienext.MainActivity
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.serializer.MovieSerializer
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        val movieRepository: MovieRepository,
        private val movieSerializer: MovieSerializer,
    ) : ViewModel() {
        private lateinit var navHostController: NavHostController

        private val _listedMovies = MutableStateFlow<AsyncMovie>(AsyncMovie.Loading)
        val listedMovies: StateFlow<AsyncMovie> = _listedMovies.asStateFlow()

        private val _popupInfo: MutableStateFlow<PopupInfo> = MutableStateFlow(PopupInfo.None)
        val popupInfo: StateFlow<PopupInfo> = _popupInfo.asStateFlow()

        private val _exportRequest = MutableSharedFlow<String>()
        val exportRequest = _exportRequest.asSharedFlow()

        private var lastIntent: Intent? = null

        fun attachNavHostController(navHostController: NavHostController) {
            this.navHostController = navHostController
        }

        fun showPopup(popupInfo: PopupInfo) {
            _popupInfo.value = popupInfo
        }

        fun closePopup() {
            _popupInfo.value = PopupInfo.None
        }

        fun closePopupOfType(popupType: KClass<out PopupInfo>) {
            if (popupType.isInstance(_popupInfo.value)) {
                closePopup()
            }
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

        internal fun updateMovies(movies: AsyncMovie) {
            viewModelScope.launch {
                _listedMovies.value = movies
            }
        }

        fun loadAndNavigateTo(linkRoute: String) {
            viewModelScope.launch {
                val tmdbId =
                    Regex("\\d+").find(linkRoute)?.value?.toLongOrNull() ?: run {
                        showPopup(PopupInfo.MovieNotFound)
                        return@launch
                    }

                val movieId = movieRepository.fetchMovieIdFromTmdbId(tmdbId)
                if (movieId != null) {
                    onNavigateWithParam(Routes.SingleMovieView, movieId, popToHome = true)
                } else {
                    onNavigateWithParam(Routes.SharedMovieView, tmdbId, popToHome = true)
                }
            }
        }

        fun parseIntent(intent: Intent?) {
            val uri = intent?.data ?: return
            val path = uri.path ?: return

            // prevent duplicate re-open
            if (intent == lastIntent) {
                return
            }
            lastIntent = intent

            if (uri.scheme == BuildConfig.SHARE_SCHEME && uri.host == BuildConfig.SHARE_HOST) {
                loadAndNavigateTo(path)
            }
        }

        fun requestExport(suggestedFilename: String) {
            viewModelScope.launch {
                _exportRequest.emit(suggestedFilename)
            }
        }

        fun saveJsonData(
            activity: MainActivity,
            uri: Uri,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        ) {
            viewModelScope.launch(ioDispatcher) {
                val jsonToSave = movieSerializer.fullMoviesJson()
                val result: PopupInfo =
                    try {
                        activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonToSave.toByteArray())
                        }
                        PopupInfo.ExportSaved
                    } catch (e: IOException) {
                        e.printStackTrace()
                        PopupInfo.ExportSaveFailed
                    }
                showPopup(result)
            }
        }
    }
