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
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MovieListDisplayState
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModel
    @Inject
    constructor() : ViewModel() {
        private val _movieListDisplayState = MutableStateFlow(MovieListDisplayState())
        val movieListDisplayState: StateFlow<MovieListDisplayState> =
            _movieListDisplayState.asStateFlow()

        private val _popupInfo: MutableStateFlow<PopupInfo> = MutableStateFlow(PopupInfo.None)
        val popupInfo: StateFlow<PopupInfo> = _popupInfo.asStateFlow()

        fun setListMode(listMode: ListMode) {
            _movieListDisplayState.update { it.copy(listMode = listMode) }
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
    }
