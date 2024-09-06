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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.util.dpf
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortingMenu(
    sortingSetup: SortingSetup,
    onSelectAction: (SortingCriteria, SortingDirection) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 8.dp)
                .testTag(UiTags.Menus.SORTING),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SortingCriteria.entries
            .map { criteria ->
                SortingButton(
                    criteria = criteria,
                    direction = sortingSetup.direction,
                    isSelected = criteria == sortingSetup.criteria,
                    onSelectAction = onSelectAction,
                )
            }
    }
}

@Composable
fun SortingButton(
    criteria: SortingCriteria,
    direction: SortingDirection,
    isSelected: Boolean,
    onSelectAction: (SortingCriteria, SortingDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }

    val readState =
        when {
            !isSelected -> stringResource(R.string.is_unselected)
            direction == SortingDirection.Ascending -> stringResource(R.string.ascending)
            else -> stringResource(R.string.descending)
        }
    val readableText =
        when {
            criteria == SortingCriteria.Random ->
                stringResource(R.string.shuffle) + ". " +
                    if (isSelected) stringResource(R.string.is_selected) else stringResource(R.string.is_unselected)

            criteria.readable != null ->
                stringResource(criteria.readable) + readState

            else ->
                stringResource(R.string.sort_by, stringResource(criteria.display)) + readState
        }
    Box(
        modifier =
            modifier
                .padding(3.dp)
                .size(width = 110.dpf, height = 28.dpf)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).clickable {
                    if (isSelected) {
                        onSelectAction(criteria, direction.opposite())
                    } else {
                        onSelectAction(criteria, SortingDirection.Default)
                    }
                }.semantics { contentDescription = readableText }
                .testTag(criteria.tag),
    ) {
        Text(
            text = stringResource(id = criteria.display),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier =
                Modifier
                    .padding(5.dpf)
                    .padding(start = 2.dpf, end = 2.dpf)
                    .size(100.dpf)
                    .clearAndSetSemantics {},
        )
        // do not show arrow for random
        if (isSelected && criteria != SortingCriteria.Random) {
            Icon(
                if (direction == SortingDirection.Ascending) {
                    Icons.Outlined.ArrowUpward
                } else {
                    Icons.Outlined.ArrowDownward
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .size(12.dp)
                        .align(Alignment.CenterEnd),
            )
        }
    }
}
