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
}
