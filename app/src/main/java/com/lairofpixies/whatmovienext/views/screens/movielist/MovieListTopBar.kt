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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.views.components.CustomTopBar
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.QuickFind
import kotlinx.coroutines.flow.Flow
import kotlin.math.min

@Composable
fun MovieListTopBar(
    triggerBar: MutableState<Boolean>,
    isArchiveVisitable: Boolean,
    onOpenArchive: () -> Unit,
    quickFind: QuickFind,
    onQuickFindTextUpdated: (String) -> Unit,
    onQuickFindTrigger: () -> Unit,
    focusEvent: Flow<Boolean>,
    modifier: Modifier = Modifier,
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(triggerBar.value) {
        if (!triggerBar.value) {
            softwareKeyboardController?.hide()
        }
    }

    LaunchedEffect(focusEvent) {
        focusEvent.collect { activate ->
            if (activate) {
                triggerBar.value = true
                focusRequester.requestFocus()
                softwareKeyboardController?.show()
            }
        }
    }

    CustomTopBar(
        trigger = triggerBar,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomSearchBar(
                query = quickFind.query,
                onQueryChange = onQuickFindTextUpdated,
                onSearch = onQuickFindTrigger,
                onClose = {
                    onQuickFindTextUpdated("")
                    triggerBar.value = false
                },
                currentMatch = quickFind.matchIndex,
                matchCount = quickFind.matches.size,
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f),
            )
            Icon(
                ButtonSpec.ArchiveShortcut.icon,
                contentDescription = stringResource(ButtonSpec.ArchiveShortcut.labelRes),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .alpha(if (isArchiveVisitable) 0.8f else 0.4f)
                        .clickable { if (isArchiveVisitable) onOpenArchive() }
                        .testTag(UiTags.Buttons.ARCHIVE_SHORTCUT),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClose: () -> Unit,
    matchCount: Int,
    currentMatch: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(32.dp)
                .focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        decorationBox = { innerTextField ->
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        ).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (query.isEmpty()) Icons.Outlined.Search else Icons.Outlined.Close,
                    contentDescription = if (query.isEmpty()) stringResource(R.string.find) else stringResource(R.string.clear_find),
                    modifier =
                        Modifier
                            .size(20.dp)
                            .clickable { if (query.isNotEmpty()) onClose() },
                )
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                ) {
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.find),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    val currentMatchDisplay = min(currentMatch + 1, matchCount)
                    Text(
                        text = "$currentMatchDisplay/$matchCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onSearch() },
                    )
                }
            }
        },
    )
}
