package com.lairofpixies.whatmovienext.viewmodel

import com.lairofpixies.whatmovienext.R

enum class ErrorState(
    val messageResource: Int = -1,
) {
    None,
    SavingWithEmptyTitle(R.string.error_title_is_required),
}
