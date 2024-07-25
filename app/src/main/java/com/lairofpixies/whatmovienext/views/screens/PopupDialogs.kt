package com.lairofpixies.whatmovienext.views.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
        PopupInfo.SavingWithEmptyTitle ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.SAVING_WITH_EMPTY_TITLE),
                bodyRes = R.string.error_title_is_required,
                onDismiss = onDismiss,
            )

        is PopupInfo.UnsavedChanges ->
            ThreeButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.UNSAVED_CHANGES),
                bodyRes = R.string.warning_changes_not_saved,
                saveLabelRes = R.string.save,
                onSave = popupInfo.onSave,
                discardLabelRes = R.string.discard,
                onDiscard = popupInfo.onDiscard,
                dismissLabelRes = R.string.continue_editing,
                onDismiss = onDismiss,
            )

        is PopupInfo.DuplicatedTitle ->
            ThreeButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.DUPLICATED_TITLE),
                bodyRes = R.string.error_title_already_exists,
                saveLabelRes = R.string.overwrite,
                onSave = popupInfo.onSave,
                discardLabelRes = R.string.discard_changes,
                onDiscard = popupInfo.onDiscard,
                dismissLabelRes = R.string.continue_editing,
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
    }
}

@Composable
fun SingleButtonDialog(
    @StringRes bodyRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.error_title)) },
        text = { Text(context.getString(bodyRes)) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(context.getString(R.string.close))
            }
        },
    )
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
    val context = LocalContext.current
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(context.getString(titleRes)) },
        text = { Text(context.getString(contentRes)) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(context.getString(confirmRes))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(context.getString(dismissRes))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeButtonDialog(
    @StringRes bodyRes: Int,
    @StringRes saveLabelRes: Int,
    onSave: () -> Unit,
    @StringRes discardLabelRes: Int,
    onDiscard: () -> Unit,
    @StringRes dismissLabelRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
    ) {
        Box {
            Column {
                Text(context.getString(bodyRes))
                Row {
                    Button(onClick = {
                        onSave()
                        onDismiss()
                    }) {
                        Text(context.getString(saveLabelRes))
                    }
                    Button(onClick = {
                        onDiscard()
                        onDismiss()
                    }) {
                        Text(context.getString(discardLabelRes))
                    }
                    Button(onClick = onDismiss) {
                        Text(context.getString(dismissLabelRes))
                    }
                }
            }
        }
    }
}
