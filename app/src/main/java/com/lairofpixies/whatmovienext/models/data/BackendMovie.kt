package com.lairofpixies.whatmovienext.models.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackendMovie(
    val title: String,
)
