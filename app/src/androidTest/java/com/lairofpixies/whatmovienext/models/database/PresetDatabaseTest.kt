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
package com.lairofpixies.whatmovienext.models.database

import com.lairofpixies.whatmovienext.models.database.data.DbPreset
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class PresetDatabaseTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: MovieDatabase

    @Inject
    lateinit var presetDao: PresetDao

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insert read and delete presets`() =
        runTest {
            // Given
            val preset =
                DbPreset(
                    presetId = 100,
                    name = "test preset",
                    sortingCriteria = SortingCriteria.Runtime,
                    sortingDirection = SortingDirection.Ascending,
                    listMode = ListMode.WATCHED,
                    minYear = 2001,
                    maxYear = 2009,
                    minRuntime = 120,
                    maxRuntime = 200,
                    minRtScore = 50,
                    maxRtScore = 70,
                    minMcScore = 30,
                    maxMcScore = 80,
                )

            // insert
            val insertedId = presetDao.insertOrUpdate(preset)
            assertEquals(100L, insertedId)

            // read
            val single = presetDao.getPreset(100).firstOrNull()
            assertEquals(preset, single)
            val asList = presetDao.getAllPresets().firstOrNull()
            assertEquals(listOf(preset), asList)

            // delete
            presetDao.deletePreset(preset)
            assertEquals(null, presetDao.getPreset(100).firstOrNull())
        }
}
