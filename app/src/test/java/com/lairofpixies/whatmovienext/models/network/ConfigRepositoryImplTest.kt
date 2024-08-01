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
package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.models.data.ImagePaths
import com.lairofpixies.whatmovienext.models.preferences.AppPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigRepositoryImplTest {
    private lateinit var appPreferences: AppPreferences
    private lateinit var configRepository: ConfigRepository

    @Before
    fun setUp() {
        appPreferences =
            mockk(relaxed = true) {
                every { imagePaths() } returns
                    flowOf(
                        ImagePaths(
                            baseUrl = "https://image.tmdb.org/t/p/",
                            thumbnailPath = "w154",
                            coverPath = "w500",
                        ),
                    )
            }
    }

    private fun TestScope.initializeSut() {
        configRepository =
            ConfigRepositoryImpl(
                appPreferences,
                UnconfinedTestDispatcher(testScheduler),
            )
        configRepository.trackConfiguration()
        advanceUntilIdle()
    }

    @Test
    fun getThumbnailUrl() =
        runTest {
            initializeSut()
            val url = configRepository.getThumbnailUrl("/test")
            assertEquals("https://image.tmdb.org/t/p/w154/test", url)
        }

    @Test
    fun getCoverUrl() =
        runTest {
            initializeSut()
            val url = configRepository.getCoverUrl("/test")
            assertEquals("https://image.tmdb.org/t/p/w500/test", url)
        }
}
