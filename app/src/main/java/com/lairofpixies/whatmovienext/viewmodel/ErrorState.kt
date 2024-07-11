package com.lairofpixies.whatmovienext.viewmodel

import com.lairofpixies.whatmovienext.R

sealed class ErrorState {
    data object None : ErrorState()

    data object SavingWithEmptyTitle : ErrorState() {
        val messageResource: Int = R.string.error_title_is_required
    }

    data class UnsavedChanges(
        val onSave: () -> Unit,
        val onDiscard: () -> Unit,
    ) : ErrorState() {
        val messageResource: Int = R.string.warning_changes_not_saved
    }
}
