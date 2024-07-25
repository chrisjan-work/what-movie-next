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

        fun clearPopup() {
            _popupInfo.value = PopupInfo.None
        }
    }
