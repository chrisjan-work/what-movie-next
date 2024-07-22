package com.lairofpixies.whatmovienext.views.state

import com.lairofpixies.whatmovienext.models.data.Movie

enum class ListMode {
    ALL,
    WATCHED,
    PENDING,
    ;

    fun next(): ListMode = entries[(ordinal + 1) % entries.size]
}

data class UiState(
    val movieList: List<Movie> = emptyList(),
    val listMode: ListMode = ListMode.ALL,
    val archiveList: List<Movie> = emptyList(),
    val errorState: ErrorState = ErrorState.None,
)
