package com.lairofpixies.whatmovienext.models.data

sealed class DownloadMovieInfo {
    data object Loading : DownloadMovieInfo()

    data class Failed(
        val trowable: Throwable,
    ) : DownloadMovieInfo()

    data object Empty : DownloadMovieInfo()

    data class Single(
        val movie: Movie,
    ) : DownloadMovieInfo()

    data class Multiple(
        val movies: List<Movie>,
    ) : DownloadMovieInfo()
}
