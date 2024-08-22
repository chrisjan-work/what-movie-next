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
package com.lairofpixies.whatmovienext.views.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import kotlin.math.max
import kotlin.math.min

@Composable
fun PopupDialogs(
    modifier: Modifier = Modifier,
    popupInfo: PopupInfo,
    onDismiss: () -> Unit,
) {
    when (popupInfo) {
        PopupInfo.None -> {}
        PopupInfo.EmptyTitle ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.EMPTY_TITLE),
                titleRes = R.string.missing_title_title,
                contentRes = R.string.error_title_is_required,
                onDismiss = onDismiss,
            )

        is PopupInfo.ConfirmDeletion ->
            TwoButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.CONFIRM_DELETION),
                titleRes = R.string.delete_forever,
                contentRes = R.string.please_confirm_deletion,
                confirmRes = R.string.confirm_deletion,
                dismissRes = R.string.cancel,
                onConfirm = popupInfo.onConfirm,
                onDismiss = onDismiss,
            )

        is PopupInfo.Searching ->
            ProgressDialog(
                modifier = modifier.testTag(UiTags.Popups.SEARCHING),
                contentRes = R.string.search_in_progress,
                onDismiss = {
                    popupInfo.onCancel()
                    onDismiss()
                },
            )

        is PopupInfo.SearchEmpty ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.SEARCH_EMPTY),
                titleRes = R.string.search_empty_title,
                contentRes = R.string.search_empty_explanation,
                onDismiss = onDismiss,
            )

        is PopupInfo.ConnectionFailed ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.CONNECTION_FAILED),
                titleRes = R.string.connection_failed_title,
                contentRes = R.string.connection_failed_explanation,
                onDismiss = onDismiss,
            )

        is PopupInfo.NumberChooser ->
            NumberChooserDialog(
                label = popupInfo.label,
                filterValues = popupInfo.filterValues,
                range = popupInfo.range,
                valueToText = popupInfo.valueToText,
                textToValue = popupInfo.textToValue,
                onConfirm = popupInfo.onConfirm,
                onDismiss = onDismiss,
            )
    }
}

@Composable
fun SingleButtonDialog(
    @StringRes contentRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int? = null,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes ?: R.string.alertdialog_title)) },
        text = { Text(stringResource(contentRes)) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
fun ProgressDialog(
    @StringRes contentRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        stringResource(contentRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlertDialogDefaults.textContentColor,
                    )
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun TwoButtonDialog(
    @StringRes titleRes: Int,
    @StringRes contentRes: Int,
    @StringRes confirmRes: Int,
    @StringRes dismissRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(contentRes)) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(confirmRes))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(dismissRes))
            }
        },
    )
}

@Composable
fun NumberChooserDialog(
    label: String,
    filterValues: MinMaxFilter,
    range: IntRange,
    valueToText: (Int?) -> String,
    textToValue: (String) -> Int?,
    onConfirm: (MinMaxFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentValues = remember { mutableStateOf(filterValues) }

    val minInputText = remember { mutableStateOf(valueToText(currentValues.value.min)) }
    val commitMinText: (String) -> Unit = {
        val newMin = textToValue(it)?.coerceIn(range)
        val currentMax = currentValues.value.max
        currentValues.value =
            if (newMin != null && currentMax != null) {
                MinMaxFilter(min(newMin, currentMax), max(newMin, currentMax))
            } else {
                MinMaxFilter(newMin, currentMax)
            }
        minInputText.value = valueToText(currentValues.value.min)
    }

    val maxInputText = remember { mutableStateOf(valueToText(currentValues.value.max)) }
    val commitMaxText: (String) -> Unit = { it ->
        val newMax = textToValue(it)?.coerceIn(range)
        val currentMin = currentValues.value.min
        currentValues.value =
            if (newMax != null && currentMin != null) {
                MinMaxFilter(min(currentMin, newMax), max(currentMin, newMax))
            } else {
                MinMaxFilter(currentMin, newMax)
            }
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
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.align(Alignment.End).fillMaxWidth(),
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    OutlinedButton(onClick = {
                        currentValues.value = MinMaxFilter(null, null)
                        minInputText.value = "-"
                        maxInputText.value = "-"
                    }) {
                        Text(stringResource(R.string.reset))
                    }
                    Button(
                        onClick = {
                            commitMinText(minInputText.value)
                            commitMaxText(maxInputText.value)
                            onConfirm(currentValues.value)
                            onDismiss()
                        },
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
