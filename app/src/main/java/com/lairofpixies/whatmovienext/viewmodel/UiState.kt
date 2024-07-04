package com.lairofpixies.whatmovienext.viewmodel

import com.lairofpixies.whatmovienext.database.Movie

data class UiState(
    val movieList: List<Movie> = emptyList(),
    val expandedMovie: Movie? = null,
)
