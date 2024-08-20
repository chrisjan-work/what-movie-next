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
package com.lairofpixies.whatmovienext.views.screens.movielist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.views.components.CustomTopBar
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.screens.UiTags

@Composable
fun MovieListTopBar(
    trigger: State<Boolean>,
    isArchiveVisitable: Boolean,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CustomTopBar(
        trigger = trigger,
        modifier = modifier,
    ) {
        Icon(
            ButtonSpec.ArchiveShortcut.icon,
            contentDescription = stringResource(ButtonSpec.ArchiveShortcut.labelRes),
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
                    .alpha(if (isArchiveVisitable) 0.8f else 0.4f)
                    .clickable { if (isArchiveVisitable) onOpenArchive() }
                    .testTag(UiTags.Buttons.ARCHIVE_SHORTCUT),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}
