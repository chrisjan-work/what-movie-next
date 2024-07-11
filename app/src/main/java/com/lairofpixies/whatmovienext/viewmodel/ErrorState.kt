package com.lairofpixies.whatmovienext.viewmodel

sealed class ErrorState {
    data object None : ErrorState()

    data object SavingWithEmptyTitle : ErrorState()

    data class UnsavedChanges(
        val onSave: () -> Unit,
        val onDiscard: () -> Unit,
    ) : ErrorState()
}
