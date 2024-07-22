package com.lairofpixies.whatmovienext.models.data

sealed class PartialMovie {
    data object Loading : PartialMovie()

    data object NotFound : PartialMovie()

    data class Completed(
        val movie: Movie,
    ) : PartialMovie()
}
