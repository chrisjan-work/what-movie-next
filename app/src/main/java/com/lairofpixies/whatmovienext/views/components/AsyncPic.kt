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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.views.state.AsyncPicState
import com.lairofpixies.whatmovienext.views.state.AsyncPicState.Loading
import com.lairofpixies.whatmovienext.views.state.AsyncPicState.Missing
import com.lairofpixies.whatmovienext.views.state.AsyncPicState.Start
import com.lairofpixies.whatmovienext.views.state.AsyncPicState.Success

@Composable
fun AsyncPic(
    url: String,
    contentDescription: String?,
    placeholderIcon: ImageVector,
    width: Dp,
    height: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val asyncPicState = remember { mutableStateOf(Start) }

    LaunchedEffect(url) {
        if (url.isBlank()) {
            asyncPicState.value = Missing
        }
    }

    Box(
        modifier =
            modifier
                .size(width = width, height = height),
    ) {
        if (asyncPicState.value != Success) {
            PicPlaceholder(
                asyncPicState = asyncPicState.value,
                icon = placeholderIcon,
                cornerRadius = cornerRadius,
                contentDescription = stringResource(R.string.missing_image),
                modifier = Modifier,
            )
        }

        if (url.isNotBlank()) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                contentDescription = contentDescription,
                onState = { state ->
                    asyncPicState.value =
                        when (state) {
                            is AsyncImagePainter.State.Loading -> Loading
                            is AsyncImagePainter.State.Success -> Success
                            is AsyncImagePainter.State.Error -> Missing
                            is AsyncImagePainter.State.Empty -> Missing
                        }
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun PicPlaceholder(
    asyncPicState: AsyncPicState,
    contentDescription: String,
    icon: ImageVector,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    if (asyncPicState == Start || asyncPicState == Success) {
        Spacer(modifier = modifier.fillMaxSize())
        return
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.33f)),
        contentAlignment = Alignment.Center,
    ) {
        if (asyncPicState == Loading) {
            CircularProgressIndicator(
                modifier = modifier.fillMaxSize(fraction = 0.5f),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
            )
        } else { // Missing
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier =
                    modifier.fillMaxSize(fraction = 0.67f),
                tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
            )
        }
    }
}
