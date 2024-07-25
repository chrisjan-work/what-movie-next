package com.lairofpixies.whatmovienext.views.state

sealed class PopupInfo {
    data object None : PopupInfo()

    data object SavingWithEmptyTitle : PopupInfo()

    data class UnsavedChanges(
        val onSave: () -> Unit,
        val onDiscard: () -> Unit,
    ) : PopupInfo()

    data class DuplicatedTitle(
        val onSave: () -> Unit,
        val onDiscard: () -> Unit,
    ) : PopupInfo()

    data class ConfirmDeletion(
        val onConfirm: () -> Unit,
    ) : PopupInfo()
}
