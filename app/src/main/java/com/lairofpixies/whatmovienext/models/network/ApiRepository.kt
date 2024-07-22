package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.DownloadMovieInfo
import kotlinx.coroutines.flow.StateFlow

interface ApiRepository {
    fun findMoviesByTitle(title: String): StateFlow<DownloadMovieInfo>
}
