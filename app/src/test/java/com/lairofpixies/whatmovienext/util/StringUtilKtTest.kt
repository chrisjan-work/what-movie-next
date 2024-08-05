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

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilKtTest {
    @Test
    fun `untouched url`() {
        assertEquals(
            "https://myhost/mypath/myfile.txt",
            "https://myhost/mypath/myfile.txt".toCanonicalUrl(),
        )
    }

    @Test
    fun `multiple slashes`() {
        assertEquals(
            "https://myhost/mypath/myfile.txt",
            "https://myhost//mypath///myfile.txt".toCanonicalUrl(),
        )
    }

    @Test
    fun `parent directory`() {
        assertEquals(
            "https://myhost/otherpath/myfile.txt",
            "https://myhost/mypath/../otherpath/myfile.txt".toCanonicalUrl(),
        )
    }

    @Test
    fun `split and rejoin list of strings`() {
        // Given
        val originalList =
            listOf(
                "Hello, World!",
                "This is a test.",
                "\"Quotes\" and, commas.",
                "Backslash: \\",
                "",
                "String with \\ and , inside",
            )
        // When
        val encodedString = originalList.encodeToString()
        val decodedList = encodedString.decodeToList()

        // Then
        assertEquals(originalList, decodedList)
    }

    @Test
    fun `manage encoding and decoding of empty list`() {
        val encodedString = emptyList<String>().encodeToString()
        val decodedList = encodedString.decodeToList()

        assertEquals(emptyList<String>(), decodedList)
    }

    @Test
    fun `printable running times`() {
        // empty
        assertEquals(
            "",
            printableRuntime(0),
        )
        // minutes
        assertEquals(
            "23 min",
            printableRuntime(23),
        )
        // hours
        assertEquals(
            "2h 3min",
            printableRuntime(123),
        )
        // pre+min+pos
        assertEquals(
            "abcd-2 min-efg",
            printableRuntime(2, "abcd-", "-efg"),
        )
        // pre+hour+pos
        assertEquals(
            "abcd-1h 0min-efg",
            printableRuntime(60, "abcd-", "-efg"),
        )
    }
}
