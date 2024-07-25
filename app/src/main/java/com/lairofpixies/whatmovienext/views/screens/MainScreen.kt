package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lairofpixies.whatmovienext.ui.theme.WhatMovieNextTheme
import com.lairofpixies.whatmovienext.viewmodels.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.NavigationHost

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val uiState = viewModel.uiState.collectAsState().value

    WhatMovieNextTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NavigationHost(
                navController = navController,
                uiState = uiState,
                mainViewModel = viewModel,
            )

            PopupDialogs(
                popupInfo = uiState.popupInfo,
            ) {
                viewModel.clearPopup()
            }
        }
    }
}
