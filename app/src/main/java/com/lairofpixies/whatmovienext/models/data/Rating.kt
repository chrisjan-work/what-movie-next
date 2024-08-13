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
package com.lairofpixies.whatmovienext.models.data

data class Rating(
    val source: Rater,
    val sourceId: String,
    val displayValue: String,
    val percentValue: Int,
) {
    enum class Rater(
        val displayName: String,
    ) {
        RottenTomatoes("Rotten Tomatoes"),
        Metacritic("Metacritic"),
        ;

        companion object {
            fun fromName(name: String): Rater? = entries.firstOrNull { it.displayName == name }
        }
    }

    companion object {
        fun defaultFor(rater: Rater) =
            Rating(
                source = rater,
                sourceId = "",
                displayValue = "",
                percentValue = -1,
            )
    }
}

fun Rating?.isNotNegative(): Boolean = this != null && this.percentValue >= 0

data class RatingPair(
    val rtRating: Rating = Rating.defaultFor(Rating.Rater.RottenTomatoes),
    val mcRating: Rating = Rating.defaultFor(Rating.Rater.Metacritic),
)
