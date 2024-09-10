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
package com.lairofpixies.whatmovienext

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lairofpixies.whatmovienext.viewmodels.MainViewModel
import com.lairofpixies.whatmovienext.views.screens.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        connectImportExport()

        mainViewModel.parseIntent(intent)

        setContent {
            MainScreen(mainViewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mainViewModel.parseIntent(intent)
    }

    private fun connectImportExport() {
        MainScope().launch {
            mainViewModel.exportRequest.collect { suggestedFilename ->
                exportLauncher.launch(suggestedFilename)
            }
        }

        MainScope().launch {
            mainViewModel.importRequest.collect {
                importLauncher.launch(arrayOf(MIMETYPE_JSON))
            }
        }
    }

    private val exportLauncher =
        registerForActivityResult(
            contract = ActivityResultContracts.CreateDocument(MIMETYPE_JSON),
        ) { uri ->
            uri?.let {
                mainViewModel.saveJsonData(this@MainActivity, uri)
            }
        }

    private val importLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                mainViewModel.importJsonData(this@MainActivity, uri)
            }
        }

    companion object {
        const val MIMETYPE_JSON = "application/json"
    }
}
