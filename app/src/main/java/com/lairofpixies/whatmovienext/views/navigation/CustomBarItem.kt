package com.lairofpixies.whatmovienext.views.navigation

data class CustomBarItem(
    val navigationItem: NavigationItem,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null,
) {
    constructor(
        navigationItem: NavigationItem,
        onClick: (() -> Unit)?,
    ) : this(navigationItem, true, onClick)
}
