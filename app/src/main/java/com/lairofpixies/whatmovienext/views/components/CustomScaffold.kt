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
package com.lairofpixies.whatmovienext.views.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val TOP_BAR_REFRESH_TIME_MS = 1000L

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomScaffold(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable (State<Boolean>) -> Unit = {},
    content: @Composable (PaddingValues, () -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val topbarState = remember { mutableStateOf(false) }
    val isRefreshing = remember { mutableStateOf(false) }
    val onShowTopBar: () -> Unit = {
        coroutineScope.launch {
            isRefreshing.value = true
            topbarState.value = true
            delay(TOP_BAR_REFRESH_TIME_MS)
            isRefreshing.value = false
        }
    }
    val refreshState = rememberPullRefreshState(isRefreshing.value, onRefresh = onShowTopBar)

    val onScrollEvent: () -> Unit = {
        if (!isRefreshing.value) {
            topbarState.value = false
        }
    }

    Scaffold(
        bottomBar = bottomBar,
        modifier =
            modifier
                .pullRefresh(refreshState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onShowTopBar() },
                    )
                },
    ) { padding ->
        content(padding, onScrollEvent)
        topBar(topbarState)
    }
}
