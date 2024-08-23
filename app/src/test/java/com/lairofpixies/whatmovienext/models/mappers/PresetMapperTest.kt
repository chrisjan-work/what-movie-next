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

import com.lairofpixies.whatmovienext.models.data.TestPreset.forApp
import com.lairofpixies.whatmovienext.models.data.TestPreset.forDb
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PresetMapperTest {
    private lateinit var presetMapper: PresetMapper

    @Before
    fun setUp() {
        presetMapper = PresetMapper()
    }

    @Test
    fun toPreset() {
        val converted = presetMapper.toPreset(forDb())

        assertEquals(forApp(), converted)
    }

    @Test
    fun toDbPreset() {
        val converted = presetMapper.toDbPreset(forApp())

        assertEquals(forDb(), converted)
    }

    @Test
    fun `convert runtime to text for input`() {
        val cases =
            listOf(
                null to "-",
                0 to "0",
                1 to "1 min",
                12 to "12 min",
                180 to "3h 0min",
                210 to "3h 30min",
                90 to "1h 30min",
                85 to "1h 25min",
                135 to "2h 15min",
                30 to "30 min",
                105 to "1h 45min",
            )

        cases.forEach { case ->
            assertEquals(case.second, presetMapper.runtimeToInput(case.first))
        }
    }

    @Test
    fun `convert runtime to text for button`() {
        val cases =
            listOf(
                null to "",
                0 to "0m",
                1 to "1m",
                12 to "12m",
                180 to "3h0m",
                210 to "3h30m",
                90 to "1h30m",
                85 to "1h25m",
                135 to "2h15m",
                30 to "30m",
                105 to "1h45m",
            )

        cases.forEach { case ->
            assertEquals(case.second, presetMapper.runtimeToButton(case.first))
        }
    }

    @Test
    fun `parse runtime input`() {
        val cases =
            listOf(
                "" to null,
                "-" to null,
                "0" to 0,
                "1" to 1,
                "12" to 12,
                "3h" to 180,
                "3h 30m" to 210,
                "1H 30min" to 90,
                "1:25" to 85,
                "1.3m" to 1,
                "1,3m" to 1,
                "12 min" to 12,
                "12 MIN" to 12,
                "12min" to 12,
                "12m" to 12,
                "1.5h" to 90,
                "2.25h" to 135,
                "0.5h" to 30,
                "1.75H" to 105,
                "3 h" to 180,
                "100h" to 1440,
            )

        cases.forEach { case ->
            assertEquals(case.second, presetMapper.inputToRuntime(case.first))
        }
    }

    @Test
    fun `convert year to text for input`() {
        val cases =
            listOf(
                null to "-",
                0 to "0",
                1900 to "1900",
                1989 to "1989",
                2011 to "2011",
            )

        cases.forEach { case ->
            assertEquals(case.second, presetMapper.numberToInput(case.first))
        }
    }

    @Test
    fun `convert year to text for button`() {
        val cases =
            listOf(
                null to "",
                0 to "0",
                1900 to "1900",
                1989 to "1989",
                2011 to "2011",
            )

        cases.forEach { case ->
            assertEquals(case.second, presetMapper.numberToButton(case.first))
        }
    }

    @Test
    fun `convert input to year`() {
        val cases =
            listOf(
                "asdf" to null,
                "-" to null,
                "0" to 0,
                "1" to 1901,
                "100" to 1900,
                "120" to 1900,
                "1950" to 1950,
                "2010" to 2010,
                "2045" to 2045,
                "3000" to 2100,
                "  1999   " to 1999,
            )

        cases.forEach { case ->
            assertEquals(case.second, presetMapper.inputToYear(case.first))
        }
    }

    @Test
    fun `convert input to score`() {
        val cases =
            listOf(
                "asdf" to null,
                "-" to null,
                "0" to 0,
                "-10" to 0,
                "1" to 1,
                "53" to 53,
                "100" to 100,
                "101" to 100,
                "2045" to 100,
                "   44  " to 44,
            )

        cases.forEach { case ->
            assertEquals(case.second, presetMapper.inputToScore(case.first))
        }
    }
}
