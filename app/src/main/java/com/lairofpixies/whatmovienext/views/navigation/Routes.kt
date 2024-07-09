package com.lairofpixies.whatmovienext.views.navigation

enum class Routes(
    private val path: String,
    private val argument: String? = null,
) {
    AllMoviesView(path = "movies"),
    SingleMovieView(path = "movie_details", argument = "movieId"),
    ;

    val route
        get() = path + (argument?.let { "/{$it}" } ?: "")

    fun route(param: Long) = path + (argument?.let { "/$param" } ?: "")

    val argumentOrEmpty = argument ?: ""

    companion object {
        val HOME = AllMoviesView
    }
}
