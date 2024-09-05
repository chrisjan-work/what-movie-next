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
import com.lairofpixies.whatmovienext.views.screens.popups.PopupDialogs

@Composable
fun MainScreen(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    mainViewModel.attachNavHostController(navController)

    WhatMovieNextTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NavigationHost(
                navController = navController,
                mainViewModel = mainViewModel,
            )

            PopupDialogs(
                popupInfo = mainViewModel.popupInfo.collectAsState().value,
            ) {
                mainViewModel.closePopup()
            }
        }
    }
}
