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
    val context = LocalContext.current
    when (errorState) {
        ErrorState.None -> {}
        ErrorState.SavingWithEmptyTitle ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.SAVING_WITH_EMPTY_TITLE),
                R.string.error_title_is_required,
                onDismiss = onDismiss,
            )

        is ErrorState.UnsavedChanges ->
            ThreeButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.UNSAVED_CHANGES),
                errorMessage = context.getString(R.string.warning_changes_not_saved),
                saveLabel = context.getString(R.string.save),
                onSave = errorState.onSave,
                discardLabel = context.getString(R.string.discard),
                onDiscard = errorState.onDiscard,
                dismissLabel = context.getString(R.string.continue_editing),
                onDismiss = onDismiss,
            )

        is ErrorState.DuplicatedTitle ->
            ThreeButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.DUPLICATED_TITLE),
                errorMessage = context.getString(R.string.error_title_already_exists),
                context.getString(R.string.overwrite),
                onSave = errorState.onSave,
                context.getString(R.string.discard_changes),
                onDiscard = errorState.onDiscard,
                dismissLabel = context.getString(R.string.continue_editing),
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
    modifier: Modifier = Modifier,
    errorMessageResource: Int,
    onDismiss: () -> Unit,
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
    modifier: Modifier = Modifier,
    titleStringResource: Int,
    contentStringResource: Int,
    confirmStringResource: Int,
    dismissStringResource: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
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
    modifier: Modifier = Modifier,
    errorMessage: String,
    saveLabel: String,
    onSave: () -> Unit,
    discardLabel: String,
    onDiscard: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
    ) {
        Box {
            Column {
                Text(text = errorMessage)
                Row {
                    Button(onClick = {
                        onSave()
                        onDismiss()
                    }) {
                        Text(saveLabel)
                    }
                    Button(onClick = {
                        onDiscard()
                        onDismiss()
                    }) {
                        Text(discardLabel)
                    }
                    Button(onClick = onDismiss) {
                        Text(dismissLabel)
                    }
                }
            }
        }
    }
}
