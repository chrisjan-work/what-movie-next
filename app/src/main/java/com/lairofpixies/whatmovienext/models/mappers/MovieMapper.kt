package com.lairofpixies.whatmovienext.models.mappers

import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.Movie.Companion.NEW_ID
import com.lairofpixies.whatmovienext.models.data.RemoteMovie

object MovieMapper {
    fun mapNetToApp(remoteMovie: RemoteMovie) =
        Movie(
            id = NEW_ID,
            title = remoteMovie.title,
        )
}
