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
import com.lairofpixies.whatmovienext.models.data.MovieData.NEW_ID
import com.lairofpixies.whatmovienext.models.data.MovieData.UNKNOWN_ID
import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.RatingMap
import com.lairofpixies.whatmovienext.models.data.Staff
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.data.OmdbMovieInfo
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended.TmdbCastMember
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended.TmdbCrewMember
import java.lang.NumberFormatException
import javax.inject.Inject
import kotlin.math.roundToInt

class RemoteMapper
    @Inject
    constructor(
        private val configRepo: ConfigRepository,
        private val genreRepository: GenreRepository,
    ) {
        fun toSearchMovie(tmdbMovieBasic: TmdbMovieBasic): Movie.ForSearch =
            with(tmdbMovieBasic) {
                Movie.ForSearch(
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId,
                            title = title,
                            originalTitle = originalTitle,
                            year = toYear(releaseDate),
                            thumbnailUrl = configRepo.getThumbnailUrl(posterPath),
                            coverUrl = configRepo.getCoverUrl(posterPath),
                            genres = toGenreNames(genreIds),
                        ),
                )
            }

        fun toCardMovie(
            tmdbMovieExtended: TmdbMovieExtended,
            ratings: RatingMap,
        ): Movie.ForCard =
            with(tmdbMovieExtended) {
                Movie.ForCard(
                    appData =
                        MovieData.AppData(
                            movieId = NEW_ID,
                            watchState = WatchState.PENDING,
                            isArchived = false,
                        ),
                    searchData =
                        MovieData.SearchData(
                            tmdbId = tmdbId ?: UNKNOWN_ID,
                            title = title ?: "",
                            originalTitle = originalTitle ?: "",
                            year = toYear(releaseDate),
                            thumbnailUrl = configRepo.getThumbnailUrl(posterPath),
                            coverUrl = configRepo.getCoverUrl(posterPath),
                            genres = toGenreNames(genres?.map { it.tmdbId }),
                        ),
                    detailData =
                        MovieData.DetailData(
                            imdbId = imdbId,
                            tagline = tagline ?: "",
                            plot = summary ?: "",
                            runtimeMinutes = runtime ?: 0,
                            directorNames = toDirectorNames(credits?.crew),
                            rtRating = ratings[Rating.Rater.RottenTomatoes],
                            mcRating = ratings[Rating.Rater.Metacritic],
                        ),
                    staffData =
                        MovieData.StaffData(
                            cast = toCast(credits?.cast),
                            crew = toCrew(credits?.crew),
                        ),
                )
            }

        fun toCast(cast: List<TmdbCastMember>?): List<Staff> =
            cast?.sortedBy { it.order }?.mapIndexed { index, tmdbPerson ->
                with(tmdbPerson) {
                    Staff(
                        personId = tmdbId,
                        roleId = NEW_ID,
                        name = name,
                        originalName = originalName ?: name,
                        faceUrl = configRepo.getFaceUrl(profilePath),
                        credit = character ?: "",
                        dept = Departments.Actors.department,
                        order = index + 1,
                    )
                }
            } ?: emptyList()

        private fun filterCrew(
            crew: List<TmdbCrewMember>?,
            where: Departments,
        ) = crew
            ?.filter { where.matcher(it.department, it.job) }
            ?.map { it.copy(department = where.department) }
            ?: emptyList()

        fun toCrew(crew: List<TmdbCrewMember>?): List<Staff> {
            val directors = filterCrew(crew, Departments.Directors)
            val writers = filterCrew(crew, Departments.Writers)
            return (directors + writers).mapIndexed { index, tmdbPerson ->
                with(tmdbPerson) {
                    Staff(
                        personId = tmdbId,
                        roleId = NEW_ID,
                        name = name,
                        originalName = originalName ?: name,
                        faceUrl = configRepo.getFaceUrl(profilePath),
                        credit = job ?: "",
                        dept = department ?: "",
                        order = index + 1,
                    )
                }
            }
        }

        fun toDirectorNames(crew: List<TmdbCrewMember>?): List<String> = filterCrew(crew, Departments.Directors).map { it.name }

        fun toDbGenres(tmdbGenres: TmdbGenres): List<DbGenre> =
            tmdbGenres.genres.map { tmdbGenre ->
                with(tmdbGenre) {
                    DbGenre(
                        tmdbId = tmdbId,
                        name = name,
                    )
                }
            }

        fun toGenreNames(genreIds: List<Long>?): List<String> =
            genreIds?.let { genreRepository.genreNamesByTmdbIds(genreIds) } ?: emptyList()

        fun toYear(releaseDate: String?): Int? =
            if (!releaseDate.isNullOrBlank()) {
                try {
                    Regex("(\\d{4})-\\d{2}-\\d{2}")
                        .find(releaseDate)
                        ?.groupValues
                        ?.get(1)
                        ?.toInt()
                } catch (_: StringIndexOutOfBoundsException) {
                    null
                } catch (_: NumberFormatException) {
                    null
                }
            } else {
                null
            }

        fun toRatings(omdbRatings: OmdbMovieInfo?): RatingMap {
            if (omdbRatings == null) return emptyMap()
            if (omdbRatings.success != "True") return emptyMap()
            val ratings =
                omdbRatings.ratings.mapNotNull { omdbRating ->
                    Rating.fromName(
                        omdbRating.source,
                        omdbRating.value,
                        toPercent(omdbRating.value),
                    )
                }
            return ratings.associateBy { it.source }
        }

        fun toPercent(value: String): Int {
            val asPercent =
                Regex("(\\d+)(\\.\\d+)?%")
                    .find(value)
                    ?.groupValues
                    ?.get(1)
                    ?.toIntOrNull()
            if (asPercent != null) return asPercent

            val asFraction =
                Regex("(\\d+(\\.\\d+)?)/(\\d+(\\.\\d+)?)")
                    .find(value)
                    ?.groupValues
                    ?.let { it[1].toFloatOrNull() to it[3].toFloatOrNull() }
                    ?.let { (numerator, denominator) ->
                        if (numerator != null && denominator != null) {
                            numerator * 100f / denominator
                        } else {
                            null
                        }
                    }?.roundToInt()
            if (asFraction != null) return asFraction

            val asAnAbsolute = value.toFloatOrNull()
            return asAnAbsolute?.toInt() ?: 0
        }
    }
