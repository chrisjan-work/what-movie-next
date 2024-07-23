package com.lairofpixies.whatmovienext.views.navigation

data class CustomBarItem(
    val buttonSpec: ButtonSpec,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null,
) {
    constructor(
        buttonSpec: ButtonSpec,
        onClick: (() -> Unit)?,
    ) : this(buttonSpec, true, onClick)
}
