package com.lairofpixies.whatmovienext.models.mappers

import com.lairofpixies.whatmovienext.models.data.BackendMovie
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.Movie.Companion.NEW_ID

object MovieMapper {
    fun mapNetToApp(backendMovie: BackendMovie) =
        Movie(
            id = NEW_ID,
            title = backendMovie.title,
        )
}
