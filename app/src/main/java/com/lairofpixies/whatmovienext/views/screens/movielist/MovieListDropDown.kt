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

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.screens.UiTags

@Composable
fun MovieListDrowDown(
    isExpanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    isArchiveVisitable: Boolean,
    onOpenArchive: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val archiveLabel = stringResource(ButtonSpec.ArchiveShortcut.labelRes)
    val exportLabel = stringResource(ButtonSpec.ExportShortcut.labelRes)
    val importLabel = stringResource(ButtonSpec.ImportShortcut.labelRes)

    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { setExpanded(false) },
        modifier = modifier,
        offset = DpOffset(320.dp, (-768).dp),
    ) {
        DropdownMenuItem(
            text = { Text(archiveLabel) },
            onClick = {
                setExpanded(false)
                if (isArchiveVisitable) onOpenArchive()
            },
            leadingIcon = {
                Icon(
                    ButtonSpec.ArchiveShortcut.icon,
                    contentDescription = null,
                )
            },
            enabled = isArchiveVisitable,
            modifier =
                Modifier
                    .testTag(UiTags.Buttons.ARCHIVE_SHORTCUT)
                    .semantics(mergeDescendants = true) {
                        contentDescription = archiveLabel
                    },
        )
        DropdownMenuItem(
            text = { Text(importLabel) },
            onClick = {
                setExpanded(false)
                onImport()
            },
            leadingIcon = {
                Icon(
                    ButtonSpec.ImportShortcut.icon,
                    contentDescription = null,
                )
            },
            modifier =
            Modifier
                .testTag(UiTags.Buttons.IMPORT_SHORTCUT)
                .semantics(mergeDescendants = true) {
                    contentDescription = importLabel
                },
        )
        DropdownMenuItem(
            text = { Text(exportLabel) },
            onClick = {
                setExpanded(false)
                onExport()
            },
            leadingIcon = {
                Icon(
                    ButtonSpec.ExportShortcut.icon,
                    contentDescription = null,
                )
            },
            modifier =
                Modifier
                    .testTag(UiTags.Buttons.EXPORT_SHORTCUT)
                    .semantics(mergeDescendants = true) {
                        contentDescription = exportLabel
                    },
        )

    }
}
