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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import kotlin.math.max
import kotlin.math.min

@Composable
fun NumberChooserDialog(
    label: String,
    filterValues: MinMaxFilter,
    valueToText: (Int?) -> String,
    textToValue: (String) -> Int?,
    onConfirm: (MinMaxFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentValues = remember { mutableStateOf(filterValues) }

    val minInputText = remember { mutableStateOf(valueToText(currentValues.value.min)) }
    val maxInputText = remember { mutableStateOf(valueToText(currentValues.value.max)) }
    val commitMinText: (String) -> Unit = {
        val newMin = textToValue(it)
        val currentMax = currentValues.value.max
        currentValues.value =
            if (newMin != null && currentMax != null) {
                MinMaxFilter(min(newMin, currentMax), max(newMin, currentMax), true)
            } else {
                MinMaxFilter(newMin, currentMax, true)
            }
        minInputText.value = valueToText(currentValues.value.min)
        maxInputText.value = valueToText(currentValues.value.max)
    }

    val commitMaxText: (String) -> Unit = { it ->
        val newMax = textToValue(it)
        val currentMin = currentValues.value.min
        currentValues.value =
            if (newMax != null && currentMin != null) {
                MinMaxFilter(min(currentMin, newMax), max(currentMin, newMax), true)
            } else {
                MinMaxFilter(currentMin, newMax, true)
            }
        minInputText.value = valueToText(currentValues.value.min)
        maxInputText.value = valueToText(currentValues.value.max)
    }

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                ) {
                    MinMaxTextField(
                        label = stringResource(R.string.at_least),
                        inputText = minInputText,
                        commitText = commitMinText,
                        modifier = Modifier.weight(1f),
                    )
                    Text(" - ")
                    MinMaxTextField(
                        label = stringResource(R.string.at_most),
                        inputText = maxInputText,
                        commitText = commitMaxText,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End).fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = {
                            currentValues.value = MinMaxFilter(null, null, false)
                            minInputText.value = "-"
                            maxInputText.value = "-"
                        },
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                    Button(
                        onClick = {
                            currentValues.value =
                                MinMaxFilter(
                                    textToValue(minInputText.value),
                                    textToValue(maxInputText.value),
                                    isEnabled = true,
                                )
                            onConfirm(currentValues.value)
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
fun MinMaxTextField(
    label: String,
    inputText: MutableState<String>,
    commitText: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = inputText.value,
        onValueChange = { inputText.value = it },
        label = { Text(label) },
        modifier =
            modifier
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        if (inputText.value == "-") {
                            inputText.value = ""
                        }
                    } else {
                        commitText(inputText.value)
                    }
                },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = {
                    commitText(inputText.value)
                },
            ),
    )
}
