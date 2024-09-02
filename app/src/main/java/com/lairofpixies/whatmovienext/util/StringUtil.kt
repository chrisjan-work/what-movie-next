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

import android.text.Html
import android.text.style.URLSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
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

fun String.toAnnotatedString(linkColor: Color = Color.Blue): AnnotatedString {
    val html = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    return buildAnnotatedString {
        append(html.toString())
        html.getSpans(0, html.length, URLSpan::class.java).forEach { span ->
            val start = html.getSpanStart(span)
            val end = html.getSpanEnd(span)
            addStringAnnotation(
                tag = "URL",
                annotation = span.url,
                start = start,
                end = end,
            )
            addStyle(
                style =
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                    ),
                start = start,
                end = end,
            )
        }
    }
}

fun printableYear(
    year: Int?,
    pre: String = "",
    pos: String = "",
): String = year?.let { "$pre$it$pos" } ?: ""

fun printableRuntime(
    runtimeMinutes: Int,
    pre: String = "",
    pos: String = "",
): String =
    when (runtimeMinutes) {
        0 -> ""
        in 1..59 -> "$pre$runtimeMinutes min$pos"
        else -> "$pre${runtimeMinutes / 60}h ${runtimeMinutes % 60}min$pos"
    }

fun quickMatchAll(
    query: String,
    candidate: String,
): Boolean =
    query
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .let { wordList ->
            wordList.isNotEmpty() &&
                wordList.all { candidate.contains(it, ignoreCase = true) }
        }

fun quickMatchAny(
    query: String,
    candidate: String,
): Boolean =
    query
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .let { wordList ->
            wordList.isNotEmpty() &&
                wordList.any { candidate.contains(it, ignoreCase = true) }
        }
