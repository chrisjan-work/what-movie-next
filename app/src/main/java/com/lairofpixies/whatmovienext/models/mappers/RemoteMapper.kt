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
import com.lairofpixies.whatmovienext.models.data.Rating.Rater
import com.lairofpixies.whatmovienext.models.data.RatingPair
import com.lairofpixies.whatmovienext.models.data.Staff
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.data.OmdbMovieInfo
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended.TmdbCastMember
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended.TmdbCrewMember
import com.lairofpixies.whatmovienext.models.network.data.WikidataMovieInfo
import javax.inject.Inject
import kotlin.math.roundToInt

class RemoteMapper
    @Inject
    constructor(
        private val configRepo: ConfigRepository,
        private val genreMapper: GenreMapper,
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
                            genreIds = genreIds,
                            genreNames = genreMapper.toGenreNames(genreIds),
                        ),
                )
            }

        fun toCardMovie(
            tmdbMovieExtended: TmdbMovieExtended,
            ratings: RatingPair,
        ): Movie.ForCard =
            with(tmdbMovieExtended) {
                val genreIds = genres?.map { it.tmdbId } ?: emptyList()
                Movie.ForCard(
                    appData =
                        MovieData.AppData(
                            movieId = NEW_ID,
                            watchDates = emptyList(),
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
                            genreIds = genreIds,
                            genreNames = genreMapper.toGenreNames(genreIds),
                        ),
                    detailData =
                        MovieData.DetailData(
                            imdbId = imdbId,
                            tagline = tagline ?: "",
                            plot = summary ?: "",
                            runtimeMinutes = runtime ?: 0,
                            directorNames = toDirectorNames(credits?.crew),
                            rtRating = ratings.rtRating,
                            mcRating = ratings.mcRating,
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

        fun toRatings(
            omdbRatings: OmdbMovieInfo?,
            wikidataMovieInfo: WikidataMovieInfo?,
        ): RatingPair {
            val omdbMap: Map<Rater, String> =
                run {
                    if (omdbRatings == null) return@run emptyList()
                    if (omdbRatings.success != "True") return@run emptyList()
                    omdbRatings.ratings.mapNotNull { omdbRating ->
                        Rater.fromName(omdbRating.source)?.let { rater ->
                            rater to omdbRating.value
                        }
                    }
                }.toMap()

            // get rating from omdb, fallback to wikidata, finally fall back to empty
            val rtValue =
                omdbMap[Rater.RottenTomatoes]?.let { it.ifBlank { null } }
                    ?: wikidataMovieInfo?.rottenTomatoesRating ?: ""
            val mcValue =
                omdbMap[Rater.Metacritic]?.let { it.ifBlank { null } }
                    ?: wikidataMovieInfo?.metacriticRating ?: ""

            return RatingPair(
                rtRating =
                    Rating(
                        source = Rating.Rater.RottenTomatoes,
                        sourceId = wikidataMovieInfo?.rottenTomatoesId ?: "",
                        displayValue = rtValue,
                        percentValue = toPercent(rtValue),
                    ),
                mcRating =
                    Rating(
                        source = Rating.Rater.Metacritic,
                        sourceId = wikidataMovieInfo?.metacriticId ?: "",
                        displayValue = mcValue,
                        percentValue = toPercent(mcValue),
                    ),
            )
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
            return asAnAbsolute?.toInt() ?: -1
        }
    }
