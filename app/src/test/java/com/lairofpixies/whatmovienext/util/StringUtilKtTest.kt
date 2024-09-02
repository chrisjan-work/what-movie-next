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
    fun `printable year`() {
        // empty
        assertEquals(
            "",
            printableYear(null),
        )
        // year
        assertEquals(
            "1989",
            printableYear(1989),
        )
        // pre+year
        assertEquals(
            "it is 1989",
            printableYear(1989, pre = "it is "),
        )
        // pos+year
        assertEquals(
            "1989.",
            printableYear(1989, pos = "."),
        )
        // pre+year+pos
        assertEquals(
            "->2002<-",
            printableYear(2002, pre = "->", pos = "<-"),
        )
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

    @Test
    fun `match strings all words required`() {
        // same
        assertEquals(true, quickMatchAll("abc", "abc"))
        // substring beginning
        assertEquals(true, quickMatchAll("ab", "abc"))
        // substring middle
        assertEquals(true, quickMatchAll("bc", "abcd"))
        // too long
        assertEquals(false, quickMatchAll("abc", "ab"))
        // mismatch
        assertEquals(false, quickMatchAll("bc", "acbd"))
        // gaps
        assertEquals(false, quickMatchAll("bd", "abcde"))
        // ignore case
        assertEquals(true, quickMatchAll("AB", "ab"))
        assertEquals(true, quickMatchAll("aB", "Ab"))
        assertEquals(true, quickMatchAll("ab", "aB"))
        // match all words
        assertEquals(true, quickMatchAll("ab cd", "acdemabdy"))
        // miss one word, rejected
        assertEquals(false, quickMatchAll("ab cd", "addemabdy"))
        assertEquals(false, quickMatchAll("ab cd", "acdemadby"))
        // three words
        assertEquals(true, quickMatchAll("aaa b ccc", "acccaabaaa"))
        // words in candidate
        assertEquals(true, quickMatchAll("abc", "cda aaabccc eeee"))
        // empty string
        assertEquals(false, quickMatchAll("", "abcd"))
        // space string
        assertEquals(false, quickMatchAll("   ", "a b    c"))
        // double spaces
        assertEquals(true, quickMatchAll("ab  cd", "abcd"))
    }

    @Test
    fun `match strings any word accepted`() {
        // same
        assertEquals(true, quickMatchAny("abc", "abc"))
        // substring beginning
        assertEquals(true, quickMatchAny("ab", "abc"))
        // substring middle
        assertEquals(true, quickMatchAny("bc", "abcd"))
        // too long
        assertEquals(false, quickMatchAny("abc", "ab"))
        // mismatch
        assertEquals(false, quickMatchAny("bc", "acbd"))
        // gaps
        assertEquals(false, quickMatchAny("bd", "abcde"))
        // ignore case
        assertEquals(true, quickMatchAny("AB", "ab"))
        assertEquals(true, quickMatchAny("aB", "Ab"))
        assertEquals(true, quickMatchAny("ab", "aB"))
        // match all words
        assertEquals(true, quickMatchAny("ab cd", "acdemabdy"))
        // miss one word, still accepted
        assertEquals(true, quickMatchAny("ab cd", "addemabdy"))
        assertEquals(true, quickMatchAny("ab cd", "acdemadby"))
        // three words
        assertEquals(true, quickMatchAny("aaa b ccc", "acccaabaaa"))
        // words in candidate
        assertEquals(true, quickMatchAny("abc", "cda aaabccc eeee"))
        // empty string
        assertEquals(false, quickMatchAny("", "abcd"))
        // space string
        assertEquals(false, quickMatchAny("   ", "a b    c"))
        // double spaces
        assertEquals(true, quickMatchAny("ab  cd", "abcd"))
    }
}
