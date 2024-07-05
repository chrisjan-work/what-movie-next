package com.lairofpixies.whatmovienext.viewmodel

import com.lairofpixies.whatmovienext.database.Movie

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
)
