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

import com.lairofpixies.whatmovienext.models.data.Departments
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.MovieData
import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.Staff
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.models.database.data.DbPerson
import com.lairofpixies.whatmovienext.models.database.data.DbRole
import com.lairofpixies.whatmovienext.models.database.data.DbStaff
import com.lairofpixies.whatmovienext.models.database.data.DbStaffedMovie
import com.lairofpixies.whatmovienext.util.decodeToList
import com.lairofpixies.whatmovienext.util.encodeToString
import javax.inject.Inject

class DbMapper
    @Inject
    constructor(
        private val genreMapper: GenreMapper,
    ) {
        fun toCardMovie(staffedMovie: DbStaffedMovie): Movie.ForCard =
            with(staffedMovie.movie) {
                val genreIds = genreMapper.toGenreIds(genreIds)
                Movie.ForCard(
                    appData =
                        MovieData.AppData(
                            movieId = movieId,
                            creationTime = creationTime,
                            watchDates = toWatchDates(dbWatchDates),
                            isArchived = isArchived,
                        ),
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId,
                            title = title,
                            originalTitle = originalTitle,
                            year = year,
                            thumbnailUrl = thumbnailUrl,
                            coverUrl = coverUrl,
                            genreIds = genreIds,
                            genreNames = genreMapper.toGenreNames(genreIds),
                        ),
                    detailData =
                        MovieData.DetailData(
                            imdbId = imdbId,
                            tagline = tagline,
                            plot = plot,
                            runtimeMinutes = runtimeMinutes,
                            directorNames = toDirectorNames(staffedMovie.staff),
                            rtRating = toRtRating(rtId, rtRating),
                            mcRating = toMcRating(mcId, mcRating),
                        ),
                    staffData = toStaffData(staffedMovie.staff),
                )
            }

        fun toStaffData(dbStaffers: List<DbStaff>): MovieData.StaffData {
            val cast = mutableListOf<Staff>()
            val crew = mutableListOf<Staff>()
            dbStaffers.map { dbStaff -> toStaff(dbStaff) }.forEach { staff ->
                if (staff.dept == Departments.Actors.department) {
                    cast.add(staff)
                } else {
                    crew.add(staff)
                }
            }
            return MovieData.StaffData(
                cast = cast.sortedBy { it.order },
                crew = crew.sortedBy { it.order },
            )
        }

        fun toStaff(dbStaff: DbStaff): Staff =
            with(dbStaff) {
                Staff(
                    personId = person.personId,
                    roleId = role.roleId,
                    name = person.name,
                    originalName = person.originalName,
                    faceUrl = person.faceUrl,
                    credit = role.credit,
                    dept = role.dept,
                    order = role.order,
                )
            }

        fun toDirectorNames(dbStaffers: List<DbStaff>): List<String> =
            dbStaffers
                .filter { Departments.Directors.matcher(it.role.dept, it.role.credit) }
                .map { it.person.name }

        fun toListMovie(dbMovie: DbMovie): Movie.ForList =
            with(dbMovie) {
                val genreIds = genreMapper.toGenreIds(genreIds)
                Movie.ForList(
                    appData =
                        MovieData.AppData(
                            movieId = movieId,
                            creationTime = creationTime,
                            watchDates = toWatchDates(dbWatchDates),
                            isArchived = isArchived,
                        ),
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId,
                            title = title,
                            originalTitle = originalTitle,
                            year = year,
                            thumbnailUrl = thumbnailUrl,
                            coverUrl = coverUrl,
                            genreIds = genreIds,
                            genreNames = genreMapper.toGenreNames(genreIds),
                        ),
                    detailData =
                        MovieData.DetailData(
                            imdbId = imdbId,
                            tagline = tagline,
                            plot = plot,
                            runtimeMinutes = runtimeMinutes,
                            directorNames = toDirectorNames(directorNames),
                            rtRating = toRtRating(rtId, rtRating),
                            mcRating = toMcRating(mcId, mcRating),
                        ),
                )
            }

        fun toDbMovie(cardMovie: Movie.ForCard): DbMovie =
            with(cardMovie) {
                DbMovie(
                    movieId = appData.movieId,
                    creationTime = appData.creationTime,
                    tmdbId = searchData.tmdbId,
                    imdbId = detailData.imdbId,
                    title = searchData.title,
                    originalTitle = searchData.originalTitle,
                    year = searchData.year,
                    thumbnailUrl = searchData.thumbnailUrl,
                    coverUrl = searchData.coverUrl,
                    tagline = detailData.tagline,
                    plot = detailData.plot,
                    genreIds = genreMapper.toDbGenreIds(searchData.genreIds),
                    runtimeMinutes = detailData.runtimeMinutes,
                    directorNames = toDbDirectorNames(detailData.directorNames),
                    rtId = detailData.rtRating.sourceId,
                    rtRating = detailData.rtRating.percentValue,
                    mcId = detailData.mcRating.sourceId,
                    mcRating = detailData.mcRating.percentValue,
                    dbWatchDates = toDbWatchDates(appData.watchDates),
                    isArchived = appData.isArchived,
                )
            }

        fun toDbPeople(people: List<Staff>): List<DbPerson> =
            people.map { person ->
                with(person) {
                    DbPerson(
                        personId = personId,
                        name = name,
                        originalName = originalName,
                        faceUrl = faceUrl,
                    )
                }
            }

        fun toDbRoles(
            movieId: Long,
            people: List<Staff>,
        ): List<DbRole> =
            people.map { person ->
                with(person) {
                    DbRole(
                        personId = personId,
                        movieId = movieId,
                        credit = credit,
                        dept = dept,
                        order = order,
                    )
                }
            }

        fun toDirectorNames(names: String): List<String> = names.decodeToList()

        fun toDbDirectorNames(directorNames: List<String>): String = directorNames.encodeToString()

        fun toRtRating(
            rtId: String,
            rtRating: Int?,
        ): Rating =
            rtRating?.let {
                Rating(
                    source = Rating.Rater.RottenTomatoes,
                    sourceId = rtId,
                    percentValue = rtRating,
                    displayValue = "$rtRating%",
                )
            } ?: Rating.defaultFor(Rating.Rater.RottenTomatoes)

        fun toMcRating(
            mcId: String,
            mcRating: Int?,
        ): Rating =
            mcRating?.let {
                Rating(
                    source = Rating.Rater.Metacritic,
                    sourceId = mcId,
                    percentValue = mcRating,
                    displayValue = "$mcRating/100",
                )
            } ?: Rating.defaultFor(Rating.Rater.RottenTomatoes)

        fun toDbWatchDates(watchDates: List<Long>): String = watchDates.joinToString(",")

        fun toWatchDates(dbWatchDates: String): List<Long> = dbWatchDates.split(",").mapNotNull { it.toLongOrNull() }
    }
