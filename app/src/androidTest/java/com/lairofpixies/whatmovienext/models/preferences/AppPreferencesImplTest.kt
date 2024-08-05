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
package com.lairofpixies.whatmovienext.models.preferences

import com.lairofpixies.whatmovienext.models.data.ImagePaths
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class AppPreferencesImplTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var appPreferences: AppPreferences

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load and save lastCheckedDateMillis`() =
        runTest {
            // Given
            val changes = listOf(10L, 150L, 200L)
            val result = mutableListOf<Long>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                appPreferences.lastCheckedDateMillis(0L).toList(result)
            }

            // When
            changes.forEach { amount ->
                appPreferences.updateLastCheckedDateMillis(amount)
            }

            // Then
            assertEquals(changes, result.takeLast(changes.size))
        }

    @Test
    fun `load and save imagePaths`() =
        runTest {
            // Given
            var result: ImagePaths? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                appPreferences.imagePaths().collect {
                    result = it
                }
            }

            // When
            val imagePaths =
                ImagePaths(
                    "https://images.net",
                    "thumb",
                    "cover",
                    "face",
                )
            appPreferences.updateImagePaths(imagePaths)

            // Then
            assertEquals(imagePaths, result)
        }
}
