package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.BackendMovie
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {
    @GET("find")
    suspend fun findMoviesByTitle(
        @Query("title") title: String,
    ): List<BackendMovie>
}
