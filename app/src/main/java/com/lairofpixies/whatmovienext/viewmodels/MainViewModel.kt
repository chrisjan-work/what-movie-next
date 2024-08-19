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
import androidx.lifecycle.viewModelScope
import com.lairofpixies.whatmovienext.models.data.AsyncMovie
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModel
    @Inject
    constructor() : ViewModel() {
        private val _listedMovies = MutableStateFlow<AsyncMovie>(AsyncMovie.Loading)
        val listedMovies: StateFlow<AsyncMovie> = _listedMovies.asStateFlow()

        private val _popupInfo: MutableStateFlow<PopupInfo> = MutableStateFlow(PopupInfo.None)
        val popupInfo: StateFlow<PopupInfo> = _popupInfo.asStateFlow()

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

        internal fun updateMovies(movies: AsyncMovie) {
            viewModelScope.launch {
                _listedMovies.value = movies
            }
        }
    }
