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
package com.lairofpixies.whatmovienext.util

import java.net.URI

private const val DELIMITER = ','
private const val ESCAPE_CHAR = '\\'

fun List<String>.encodeToString(): String =
    joinToString(separator = DELIMITER.toString()) { str ->
        str
            .replace(ESCAPE_CHAR.toString(), ESCAPE_CHAR.toString() + ESCAPE_CHAR)
            .replace(DELIMITER.toString(), ESCAPE_CHAR.toString() + DELIMITER)
    }

fun String.decodeToList(): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var escaped = false

    for (char in this) {
        when {
            escaped -> {
                current.append(char)
                escaped = false
            }

            char == ESCAPE_CHAR -> escaped = true
            char == DELIMITER -> {
                result.add(current.toString())
                current.clear()
            }

            else -> current.append(char)
        }
    }

    if (current.isNotEmpty() || endsWith(DELIMITER)) {
        result.add(current.toString())
    }

    return result
}

fun String.toCanonicalUrl(): String = URI(this).normalize().toString()
