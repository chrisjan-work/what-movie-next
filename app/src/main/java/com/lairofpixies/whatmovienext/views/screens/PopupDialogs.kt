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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.views.state.PopupInfo

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
            Button(onClick = onDismiss) {
                Text(stringResource(dismissRes))
            }
        },
    )
}
