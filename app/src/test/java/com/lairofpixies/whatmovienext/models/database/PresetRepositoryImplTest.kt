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

import com.lairofpixies.whatmovienext.models.data.TestPreset.forApp
import com.lairofpixies.whatmovienext.models.data.TestPreset.forDb
import com.lairofpixies.whatmovienext.models.database.data.DbPreset
import com.lairofpixies.whatmovienext.models.mappers.PresetMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresetRepositoryImplTest {
    private lateinit var presetDao: PresetDao
    private lateinit var presetMapper: PresetMapper
    private lateinit var presetRepository: PresetRepository

    @Before
    fun setUp() {
        presetDao = mockk(relaxed = true)
        presetMapper = PresetMapper()
    }

    private fun TestScope.initializeSut() {
        presetRepository =
            PresetRepositoryImpl(
                presetDao,
                presetMapper,
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            )
        advanceUntilIdle()
    }

    @Test
    fun `load preset`() =
        runTest {
            // Given
            every { presetDao.getPreset(any()) } returns flowOf(forDb())

            // When
            initializeSut()
            val result = presetRepository.getPreset(37)

            // Then
            assertEquals(forApp(), result.first())
        }

    @Test
    fun `get empty`() =
        runTest {
            // Given
            every { presetDao.getPreset(any()) } returns flowOf(null)

            // When
            initializeSut()
            val result = presetRepository.getPreset(1)

            // Then
            assertEquals(null, result.first())
        }

    @Test
    fun `store preset`() =
        runTest {
            // Given
            coEvery { presetDao.insertOrUpdate(any()) } answers {
                (firstArg() as? DbPreset)?.presetId ?: 1
            }

            // When
            initializeSut()
            val storedId = presetRepository.updatePreset(forApp())

            // Then
            coVerify { presetDao.insertOrUpdate(any()) }
            assertEquals(storedId, 37L)
        }
}
