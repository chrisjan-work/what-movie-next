/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.lairofpixies.whatmovienext.models.mappers

import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.MovieData
import com.lairofpixies.whatmovienext.models.data.MovieData.NEW_ID
import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.RatingPair
import com.lairofpixies.whatmovienext.models.data.Staff
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.models.database.data.DbPerson
import com.lairofpixies.whatmovienext.models.database.data.DbRole
import com.lairofpixies.whatmovienext.models.database.data.DbStaff
import com.lairofpixies.whatmovienext.models.database.data.DbStaffedMovie
import com.lairofpixies.whatmovienext.models.network.data.OmdbMovieInfo
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo.Binding
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo.Bindings
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo.Results

fun testTmdbMovieExtended(): TmdbMovieExtended =
    TmdbMovieExtended(
        tmdbId = 99,
        imdbId = "tt100",
        title = "Terminator 2",
        originalTitle = "Terminator 2",
        posterPath = "/terminator2.jpg",
        releaseDate = "1991-10-24",
        tagline = "Hasta la vista, baby.",
        summary = "robots from the future",
        runtime = 137,
        genres = listOf(TmdbGenres.TmdbGenre(tmdbId = 28, name = "Action")),
        credits =
            TmdbMovieExtended.TmdbCredits(
                cast =
                    listOf(
                        TmdbMovieExtended.TmdbCastMember(
                            tmdbId = 2000,
                            name = "Solsonegene",
                            originalName = "Arnol Solsonegene",
                            profilePath = "/solsonesonegene.jpg",
                            character = "the good terminator",
                            order = 1,
                        ),
                    ),
                crew =
                    listOf(
                        TmdbMovieExtended.TmdbCrewMember(
                            tmdbId = 3000,
                            name = "Cameron",
                            originalName = "James Cameron",
                            profilePath = "/titanic.jpg",
                            job = "director",
                            department = "Directing",
                        ),
                    ),
            ),
    )

fun testOmdbMovieRatings(): OmdbMovieInfo =
    OmdbMovieInfo(
        success = "True",
        ratings =
            listOf(
                OmdbMovieInfo.OmdbRating(
                    source = "Rotten Tomatoes",
                    value = "81%",
                ),
                OmdbMovieInfo.OmdbRating(
                    source = "Metacritic",
                    value = "82/100",
                ),
            ),
    )

fun testWikidataMovieRatings(): WikidataMovieInfo =
    WikidataMovieInfo(
        Results(
            listOf(
                Bindings(
                    entity = Binding("tt100", "literal"),
                    rottenTomatoesId = Binding("m/churminator_the_ii", "literal"),
                    metacriticId = Binding("movie/churminator_the_2nd", "literal"),
                    rottenTomatoesRating = Binding("81%", "literal"),
                    metacriticRating = Binding("82/100", "literal"),
                ),
            ),
        ),
    )

fun testRatingMap(): RatingPair =
    RatingPair(
        rtRating =
            Rating(
                source = Rating.Rater.RottenTomatoes,
                sourceId = "m/churminator_the_ii",
                displayValue = "81%",
                percentValue = 81,
            ),
        mcRating =
            Rating(
                source = Rating.Rater.Metacritic,
                sourceId = "movie/churminator_the_2nd",
                displayValue = "82/100",
                percentValue = 82,
            ),
    )

fun testCardMovieExtended(movieId: Long = NEW_ID): Movie.ForCard =
    Movie.ForCard(
        appData =
            MovieData.AppData(
                movieId = movieId,
                creationTime = 0,
                watchDates = emptyList(),
                isArchived = false,
            ),
        searchData =
            MovieData.SearchData(
                tmdbId = 99,
                title = "Terminator 2",
                originalTitle = "Terminator 2",
                year = 1991,
                thumbnailUrl = "thumbnail.jpg",
                coverUrl = "cover.jpg",
                genreIds = listOf(28L),
                genreNames = listOf("Action"),
            ),
        detailData =
            MovieData.DetailData(
                imdbId = "tt100",
                tagline = "Hasta la vista, baby.",
                plot = "robots from the future",
                runtimeMinutes = 137,
                directorNames = listOf("Cameron"),
                rtRating =
                    Rating(
                        source = Rating.Rater.RottenTomatoes,
                        sourceId = "m/churminator_the_ii",
                        displayValue = "81%",
                        percentValue = 81,
                    ),
                mcRating =
                    Rating(
                        source = Rating.Rater.Metacritic,
                        sourceId = "movie/churminator_the_2nd",
                        displayValue = "82/100",
                        percentValue = 82,
                    ),
            ),
        staffData =
            MovieData.StaffData(
                cast =
                    listOf(
                        Staff(
                            personId = 2000,
                            name = "Solsonegene",
                            originalName = "Arnol Solsonegene",
                            faceUrl = "/solsonesonegene.jpg",
                            credit = "the good terminator",
                            dept = "acting",
                            order = 1,
                        ),
                    ),
                crew =
                    listOf(
                        Staff(
                            personId = 3000,
                            name = "Cameron",
                            originalName = "James Cameron",
                            faceUrl = "/titanic.jpg",
                            credit = "director",
                            dept = "directing",
                            order = 1,
                        ),
                    ),
            ),
    )

fun testListMovieExtended(): Movie.ForList =
    Movie.ForList(
        appData =
            MovieData.AppData(
                movieId = NEW_ID,
                creationTime = 0,
                watchDates = emptyList(),
                isArchived = false,
            ),
        searchData =
            MovieData.SearchData(
                tmdbId = 99,
                title = "Terminator 2",
                originalTitle = "Terminator 2",
                year = 1991,
                thumbnailUrl = "thumbnail.jpg",
                coverUrl = "cover.jpg",
                genreIds = listOf(28L),
                genreNames = listOf("Action"),
            ),
        detailData =
            MovieData.DetailData(
                imdbId = "tt100",
                tagline = "Hasta la vista, baby.",
                plot = "robots from the future",
                runtimeMinutes = 137,
                directorNames = listOf("Cameron"),
                rtRating =
                    Rating(
                        source = Rating.Rater.RottenTomatoes,
                        sourceId = "m/churminator_the_ii",
                        displayValue = "81%",
                        percentValue = 81,
                    ),
                mcRating =
                    Rating(
                        source = Rating.Rater.Metacritic,
                        sourceId = "movie/churminator_the_2nd",
                        displayValue = "82/100",
                        percentValue = 82,
                    ),
            ),
    )

fun testDbMovieExtended(movieId: Long = NEW_ID): DbMovie =
    DbMovie(
        movieId = movieId,
        creationTime = 0,
        tmdbId = 99,
        imdbId = "tt100",
        title = "Terminator 2",
        originalTitle = "Terminator 2",
        year = 1991,
        thumbnailUrl = "thumbnail.jpg",
        coverUrl = "cover.jpg",
        genreIds = "28",
        tagline = "Hasta la vista, baby.",
        plot = "robots from the future",
        runtimeMinutes = 137,
        directorNames = "Cameron",
        rtId = "m/churminator_the_ii",
        rtRating = 81,
        mcId = "movie/churminator_the_2nd",
        mcRating = 82,
    )

fun testDbStaff(movieId: Long = NEW_ID): List<DbStaff> =
    listOf(
        DbStaff(
            person =
                DbPerson(
                    personId = 2000,
                    name = "Solsonegene",
                    originalName = "Arnol Solsonegene",
                    faceUrl = "/solsonesonegene.jpg",
                ),
            role =
                DbRole(
                    movieId = movieId,
                    personId = 2000,
                    credit = "the good terminator",
                    dept = "acting",
                    order = 1,
                ),
        ),
        DbStaff(
            person =
                DbPerson(
                    personId = 3000,
                    name = "Cameron",
                    originalName = "James Cameron",
                    faceUrl = "/titanic.jpg",
                ),
            role =
                DbRole(
                    movieId = movieId,
                    personId = 3000,
                    credit = "director",
                    dept = "directing",
                    order = 1,
                ),
        ),
    )

fun testDbStaffedMovieExtended(movieId: Long = NEW_ID): DbStaffedMovie =
    DbStaffedMovie(
        movie = testDbMovieExtended(movieId),
        staff = testDbStaff(movieId),
    )

fun Movie.ForCard.removeCreationTime(): Movie.ForCard = this.copy(appData = appData.copy(creationTime = 0))
