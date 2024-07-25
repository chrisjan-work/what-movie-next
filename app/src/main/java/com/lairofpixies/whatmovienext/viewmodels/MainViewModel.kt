package com.lairofpixies.whatmovienext.viewmodels

import androidx.lifecycle.ViewModel
import com.lairofpixies.whatmovienext.views.state.ErrorState
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.UiState
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
        private val _uiState = MutableStateFlow(UiState())
        val uiState: StateFlow<UiState> = _uiState.asStateFlow()

        fun setListMode(listMode: ListMode) {
            _uiState.update { it.copy(listMode = listMode) }
        }

        fun showError(errorState: ErrorState) {
            _uiState.update { it.copy(errorState = errorState) }
        }

        fun clearError() {
            _uiState.update { it.copy(errorState = ErrorState.None) }
        }
    }
