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

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

val TOP_BAR_SPACE = 48.dp

@Composable
fun CustomTopBar(
    trigger: State<Boolean>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val topBarHeightPx = with(LocalDensity.current) { TOP_BAR_SPACE.toPx() }
    val topBarOffset =
        animateIntOffsetAsState(
            targetValue =
                if (trigger.value) {
                    IntOffset.Zero
                } else {
                    IntOffset(x = 0, y = -topBarHeightPx.roundToInt())
                },
            label = "topbar offset animation",
        )

    Box(
        modifier =
            modifier
                .offset { topBarOffset.value }
                .background(MaterialTheme.colorScheme.background)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f))
                .fillMaxWidth(),
        content = content,
    )
}
