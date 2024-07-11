package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.ui.theme.WhatMovieNextTheme
import com.lairofpixies.whatmovienext.viewmodel.ErrorState
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.NavigationHost
import com.lairofpixies.whatmovienext.views.navigation.Routes

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val uiState = viewModel.uiState.collectAsState().value

    WhatMovieNextTheme {
        Scaffold(
            floatingActionButton = {
                AddMovieButton {
                    navController.navigate(Routes.CreateMovieView.route)
                }
            },
        ) { innerPadding ->
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                color = MaterialTheme.colorScheme.background,
            ) {
                NavigationHost(
                    navController = navController,
                    uiState = uiState,
                    viewModel = viewModel,
                )

                if (uiState.errorState != ErrorState.None) {
                    val message = LocalContext.current.getString(uiState.errorState.messageResource)
                    PopupDialog(errorMessage = message) {
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}

@Composable
fun AddMovieButton(callback: () -> Unit) {
    FloatingActionButton(
        onClick = callback,
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = stringResource(R.string.add_new_movie),
        )
    }
}

@Preview
@Composable
fun AddMovieButtonPreview() {
    WhatMovieNextTheme {
        AddMovieButton { }
    }
}
