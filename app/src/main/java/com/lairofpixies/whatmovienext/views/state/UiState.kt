package com.lairofpixies.whatmovienext.views.state

import com.lairofpixies.whatmovienext.models.data.Movie

data class UiState(
    val movieList: List<Movie> = emptyList(),
    val listMode: ListMode = ListMode.ALL,
    val archiveList: List<Movie> = emptyList(),
    val errorState: ErrorState = ErrorState.None,
)
