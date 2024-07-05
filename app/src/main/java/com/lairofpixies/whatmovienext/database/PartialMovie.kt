package com.lairofpixies.whatmovienext.database

sealed class PartialMovie {
    data object Loading : PartialMovie()

    data object NotFound : PartialMovie()

    data class Completed(
        val movie: Movie,
    ) : PartialMovie()
}
