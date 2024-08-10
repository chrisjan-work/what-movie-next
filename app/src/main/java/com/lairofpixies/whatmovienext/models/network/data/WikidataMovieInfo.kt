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
package com.lairofpixies.whatmovienext.models.network.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikidataMovieInfo(
    @Json(name = "results")
    val results: Results,
) {
    val entity: String
        get() =
            results.bindings
                .firstOrNull()
                ?.entity
                ?.value ?: ""

    val rottenTomatoesId: String
        get() =
            results.bindings
                .firstOrNull()
                ?.rottenTomatoesId
                ?.value ?: ""

    val rottenTomatoesRating: String
        get() =
            results.bindings
                .firstOrNull()
                ?.rottenTomatoesRating
                ?.value ?: ""

    val metacriticId: String
        get() =
            results.bindings
                .firstOrNull()
                ?.metacriticId
                ?.value ?: ""

    val metacriticRating: String
        get() =
            results.bindings
                .firstOrNull()
                ?.metacriticRating
                ?.value ?: ""

    data class Results(
        @Json(name = "bindings")
        val bindings: List<Bindings>,
    )

    data class Bindings(
        @Json(name = "entity")
        val entity: Binding?,
        @Json(name = "rottentomatoes_id")
        val rottenTomatoesId: Binding?,
        @Json(name = "rottentomatoes_rating")
        val rottenTomatoesRating: Binding?,
        @Json(name = "metacritic_id")
        val metacriticId: Binding?,
        @Json(name = "metacritic_rating")
        val metacriticRating: Binding?,
    )

    data class Binding(
        @Json(name = "value")
        val value: String,
        @Json(name = "type")
        val type: String,
    )

    companion object {
        fun sparqlQuery(imdbId: String) =
            """
            SELECT ?entity ?rottentomatoes_id ?rottentomatoes_rating ?metacritic_id ?metacritic_rating WHERE {
              ?entity p:P345 [ps:P345 "$imdbId"].

              OPTIONAL { ?entity wdt:P1258 ?rottentomatoes_id. }

              OPTIONAL {
                ?entity p:P444 ?statementP444Rt.
                ?statementP444Rt ps:P444 ?rottentomatoes_rating.
                ?statementP444Rt pq:P459 wd:Q108403393.
              }

              OPTIONAL { ?entity wdt:P1712 ?metacritic_id. }

              OPTIONAL {
                ?entity p:P444 ?statementP444Mc.
                ?statementP444Mc ps:P444 ?metacritic_rating.
                ?statementP444Mc pq:P459 wd:Q106515043.
              }
            } LIMIT 1
            """.trimIndent()
                .replace("\n", " ")
                .replace("\r", "")
                .trim()
    }
}
