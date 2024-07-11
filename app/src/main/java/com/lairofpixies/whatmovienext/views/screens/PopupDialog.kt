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
) = when (errorState) {
    ErrorState.None -> {}
    ErrorState.SavingWithEmptyTitle ->
        PopupDialog(
            R.string.error_title_is_required,
            onDismiss = onDismiss,
        )

    is ErrorState.UnsavedChanges ->
        SaveOnExitDialog(
            onSave = errorState.onSave,
            onDiscard = errorState.onDiscard,
            onDismiss = onDismiss,
        )
}

@Composable
fun PopupDialog(
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
fun SaveOnExitDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Box {
            Column {
                Text(text = context.getString(R.string.warning_changes_not_saved))
                Row {
                    Button(onClick = {
                        onSave()
                        onDismiss()
                    }) {
                        Text(context.getString(R.string.save))
                    }
                    Button(onClick = {
                        onDiscard()
                        onDismiss()
                    }) {
                        Text(context.getString(R.string.discard))
                    }
                    Button(onClick = onDismiss) {
                        Text(context.getString(R.string.continue_editing))
                    }
                }
            }
        }
    }
}
