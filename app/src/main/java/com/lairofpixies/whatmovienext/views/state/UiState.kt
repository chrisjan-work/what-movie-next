package com.lairofpixies.whatmovienext.views.state

data class UiState(
    val listMode: ListMode = ListMode.ALL,
    val popupInfo: PopupInfo = PopupInfo.None,
)
