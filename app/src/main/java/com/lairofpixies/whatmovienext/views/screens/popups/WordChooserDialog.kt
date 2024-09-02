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
package com.lairofpixies.whatmovienext.views.screens.popups

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.views.state.WordFilter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordChooserDialog(
    label: String,
    filterValues: WordFilter,
    candidates: List<String>,
    onConfirm: (WordFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentWords = remember { mutableStateOf(filterValues.words) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = AlertDialogDefaults.shape,
        ) {
            Column(
                modifier =
                    modifier
                        .padding(18.dp),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AlertDialogDefaults.textContentColor,
                )

                FlowRow(
                    modifier =
                        modifier
                            .heightIn(max = 250.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    candidates.forEach { genre ->
                        val isSelected = genre in currentWords.value
                        WordButton(
                            word = genre,
                            isSelected = isSelected,
                            onSelect = {
                                if (!isSelected) {
                                    currentWords.value += genre
                                } else {
                                    currentWords.value -= genre
                                }
                            },
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = {
                            currentWords.value = emptyList()
                        },
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                    Button(
                        onClick = {
                            onConfirm(WordFilter(currentWords.value, true))
                            onDismiss()
                        },
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                    ) {
                        Text(stringResource(R.string.update))
                    }
                }
            }
        }
    }
}

@Composable
fun WordButton(
    word: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }

    Box(
        modifier =
            modifier
                .padding(3.dp)
                .height(32.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).clickable { onSelect() },
    ) {
        // hack to force the box to fit the text when it's bold and stay that size when it isn't
        Text(
            text = word,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            modifier =
                Modifier
                    .alpha(0f)
                    .padding(top = 8.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Text(
            text = word,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
