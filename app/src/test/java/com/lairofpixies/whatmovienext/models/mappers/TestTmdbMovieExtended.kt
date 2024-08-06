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

import com.lairofpixies.whatmovienext.models.data.AMovie
import com.lairofpixies.whatmovienext.models.data.MovieData
import com.lairofpixies.whatmovienext.models.data.MovieData.NEW_ID
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended

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
        genres = listOf(TmdbGenres.TmdbGenre(tmdbId = 188, name = "Action")),
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

fun testCardMovieExtended(): AMovie.ForCard =
    AMovie.ForCard(
        appData =
            MovieData.AppData(
                id = NEW_ID,
                creationTime = 0,
                watchState = WatchState.PENDING,
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
                genres = listOf("Action"),
            ),
        detailData =
            MovieData.DetailData(
                imdbId = "tt100",
                tagline = "Hasta la vista, baby.",
                plot = "robots from the future",
                runtimeMinutes = 137,
            ),
        staffData = MovieData.StaffData(),
    )

fun testListMovieExtended(): AMovie.ForList =
    AMovie.ForList(
        appData =
            MovieData.AppData(
                id = NEW_ID,
                creationTime = 0,
                watchState = WatchState.PENDING,
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
                genres = listOf("Action"),
            ),
        detailData =
            MovieData.DetailData(
                imdbId = "tt100",
                tagline = "Hasta la vista, baby.",
                plot = "robots from the future",
                runtimeMinutes = 137,
            ),
    )

fun testDbMovieExtended(): DbMovie =
    DbMovie(
        id = NEW_ID,
        creationTime = 0,
        tmdbId = 99,
        imdbId = "tt100",
        title = "Terminator 2",
        originalTitle = "Terminator 2",
        year = 1991,
        thumbnailUrl = "thumbnail.jpg",
        coverUrl = "cover.jpg",
        genres = "Action",
        tagline = "Hasta la vista, baby.",
        plot = "robots from the future",
        runtimeMinutes = 137,
    )

fun AMovie.ForCard.removeCreationTime(): AMovie.ForCard = this.copy(appData = appData.copy(creationTime = 0))
