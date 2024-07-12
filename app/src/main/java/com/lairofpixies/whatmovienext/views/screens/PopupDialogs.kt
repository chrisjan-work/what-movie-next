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
import androidx.compose.ui.platform.LocalContext
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.viewmodel.ErrorState

@Composable
fun PopupDialogs(
    errorState: ErrorState,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    when (errorState) {
        ErrorState.None -> {}
        ErrorState.SavingWithEmptyTitle ->
            SingleButtonDialog(
                R.string.error_title_is_required,
                onDismiss = onDismiss,
            )

        is ErrorState.UnsavedChanges ->
            ThreeButtonDialog(
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
                errorMessage = context.getString(R.string.error_title_already_exists),
                context.getString(R.string.overwrite),
                onSave = errorState.onSave,
                context.getString(R.string.discard_changes),
                onDiscard = errorState.onDiscard,
                dismissLabel = context.getString(R.string.continue_editing),
                onDismiss = onDismiss,
            )
    }
}

@Composable
fun SingleButtonDialog(
    errorMessageResource: Int,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeButtonDialog(
    errorMessage: String,
    saveLabel: String,
    onSave: () -> Unit,
    discardLabel: String,
    onDiscard: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
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
