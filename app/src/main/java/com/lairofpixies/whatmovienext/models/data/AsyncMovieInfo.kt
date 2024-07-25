package com.lairofpixies.whatmovienext.models.data

sealed class AsyncMovieInfo {
    data object Loading : AsyncMovieInfo()

    data class Failed(
        val trowable: Throwable,
    ) : AsyncMovieInfo()

    data object Empty : AsyncMovieInfo()

    data class Single(
        val movie: Movie,
    ) : AsyncMovieInfo()

    data class Multiple(
        val movies: List<Movie>,
    ) : AsyncMovieInfo()

    companion object {
        fun fromList(movies: List<Movie>): AsyncMovieInfo =
            when (movies.size) {
                0 -> Empty
                1 -> Single(movies.first())
                else -> Multiple(movies)
            }
    }
}

fun AsyncMovieInfo?.isMissing(): Boolean =
    this == null ||
        this == AsyncMovieInfo.Empty ||
        this is AsyncMovieInfo.Failed

fun AsyncMovieInfo?.hasMovie(): Boolean =
    this != null &&
        (
            this is AsyncMovieInfo.Single ||
                this is AsyncMovieInfo.Multiple
        )

fun AsyncMovieInfo.toList(): List<Movie> =
    when (this) {
        is AsyncMovieInfo.Single -> listOf(movie)
        is AsyncMovieInfo.Multiple -> movies
        else -> emptyList()
    }

fun AsyncMovieInfo.filter(sieve: (Movie) -> Boolean): AsyncMovieInfo =
    when (this) {
        is AsyncMovieInfo.Single -> if (sieve(movie)) this else AsyncMovieInfo.Empty
        is AsyncMovieInfo.Multiple -> {
            val filtered = movies.filter(sieve)
            if (filtered.isEmpty()) AsyncMovieInfo.Empty else AsyncMovieInfo.fromList(filtered)
        }

        else -> this
    }
