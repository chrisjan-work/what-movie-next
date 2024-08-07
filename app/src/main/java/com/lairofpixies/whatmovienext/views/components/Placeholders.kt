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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun placeholderBackground() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.33f)

@Composable
fun ThumbnailPlaceholder(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(color = placeholderBackground())
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        val icon =
            if (isLoading) {
                Icons.Outlined.Downloading
            } else {
                Icons.Outlined.Videocam
            }
        Icon(
            imageVector = icon,
            contentDescription = "",
            modifier =
                modifier.fillMaxSize(fraction = 0.67f),
            tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun CoverPlaceholder(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(color = placeholderBackground())
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        val icon =
            if (isLoading) {
                Icons.Outlined.Downloading
            } else {
                Icons.Outlined.Theaters
            }
        Icon(
            imageVector = icon,
            contentDescription = "",
            modifier =
                modifier.fillMaxSize(fraction = 0.67f),
            tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun FacePlaceholder(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(color = placeholderBackground()),
        contentAlignment = Alignment.Center,
    ) {
        val icon =
            if (isLoading) {
                Icons.Outlined.Downloading
            } else {
                Icons.Outlined.PersonPin
            }
        Icon(
            imageVector = icon,
            contentDescription = "",
            modifier =
                modifier.fillMaxSize(fraction = 0.67f),
            tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        )
    }
}
