package com.lairofpixies.whatmovienext.views.screens

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
import com.lairofpixies.whatmovienext.views.state.ErrorState

@Composable
fun PopupDialogs(
    modifier: Modifier = Modifier,
    errorState: ErrorState,
    onDismiss: () -> Unit,
) {
    when (errorState) {
        ErrorState.None -> {}
        ErrorState.SavingWithEmptyTitle ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.SAVING_WITH_EMPTY_TITLE),
                errorMessageResource = R.string.error_title_is_required,
                onDismiss = onDismiss,
            )

        is ErrorState.UnsavedChanges ->
            ThreeButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.UNSAVED_CHANGES),
                errorMessageResource = R.string.warning_changes_not_saved,
                saveLabelResource = R.string.save,
                onSave = errorState.onSave,
                discardLabelResource = R.string.discard,
                onDiscard = errorState.onDiscard,
                dismissLabelResource = R.string.continue_editing,
                onDismiss = onDismiss,
            )

        is ErrorState.DuplicatedTitle ->
            ThreeButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.DUPLICATED_TITLE),
                errorMessageResource = R.string.error_title_already_exists,
                saveLabelResource = R.string.overwrite,
                onSave = errorState.onSave,
                discardLabelResource = R.string.discard_changes,
                onDiscard = errorState.onDiscard,
                dismissLabelResource = R.string.continue_editing,
                onDismiss = onDismiss,
            )

        is ErrorState.ConfirmDeletion ->
            TwoButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.CONFIRM_DELETION),
                titleStringResource = R.string.delete_forever,
                contentStringResource = R.string.please_confirm_deletion,
                confirmStringResource = R.string.confirm_deletion,
                dismissStringResource = R.string.cancel,
                onConfirm = errorState.onConfirm,
                onDismiss = onDismiss,
            )
    }
}

@Composable
fun SingleButtonDialog(
    errorMessageResource: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.error_title)) },
        text = { Text(context.getString(errorMessageResource)) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(context.getString(R.string.close))
            }
        },
    )
}

@Composable
fun TwoButtonDialog(
    titleStringResource: Int,
    contentStringResource: Int,
    confirmStringResource: Int,
    dismissStringResource: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(context.getString(titleStringResource)) },
        text = { Text(context.getString(contentStringResource)) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(context.getString(confirmStringResource))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(context.getString(dismissStringResource))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeButtonDialog(
    errorMessageResource: Int,
    saveLabelResource: Int,
    onSave: () -> Unit,
    discardLabelResource: Int,
    onDiscard: () -> Unit,
    dismissLabelResource: Int,
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
                Text(context.getString(errorMessageResource))
                Row {
                    Button(onClick = {
                        onSave()
                        onDismiss()
                    }) {
                        Text(context.getString(saveLabelResource))
                    }
                    Button(onClick = {
                        onDiscard()
                        onDismiss()
                    }) {
                        Text(context.getString(discardLabelResource))
                    }
                    Button(onClick = onDismiss) {
                        Text(context.getString(dismissLabelResource))
                    }
                }
            }
        }
    }
}
