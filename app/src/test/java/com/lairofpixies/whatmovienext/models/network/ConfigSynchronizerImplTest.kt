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
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.mappers.RemoteMapper
import com.lairofpixies.whatmovienext.models.network.data.TmdbConfiguration
import com.lairofpixies.whatmovienext.models.preferences.AppPreferences
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigSynchronizerImplTest {
    private lateinit var appPreferences: AppPreferences
    private lateinit var tmdbApi: TmdbApi
    private lateinit var genreRepository: GenreRepository
    private lateinit var remoteMapper: RemoteMapper
    private lateinit var connectivityTracker: ConnectivityTracker
    private lateinit var configSynchronizer: ConfigSynchronizer

    @Before
    fun setUp() {
        tmdbApi = mockk(relaxed = true)
        appPreferences = mockk(relaxed = true)
        connectivityTracker = mockk(relaxed = true)
        genreRepository = mockk(relaxed = true)
        remoteMapper = mockk(relaxed = true)

        // Feed valid paths by default
        coEvery { tmdbApi.getConfiguration() } returns testConfiguration()
        every { appPreferences.imagePaths() } returns flowOf(testStoredPaths())
        every { connectivityTracker.isOnline() } returns flowOf(true)
    }

    private fun TestScope.initializeSut() {
        configSynchronizer =
            ConfigSynchronizerImpl(
                appPreferences = appPreferences,
                tmdbApi = tmdbApi,
                genreRepository = genreRepository,
                remoteMapper = remoteMapper,
                connectivityTracker = connectivityTracker,
                cacheExpirationTimeMillis = 1000L,
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            )
        configSynchronizer.syncConfig()
        advanceUntilIdle()
    }

    private fun testConfiguration() =
        TmdbConfiguration(
            images =
                TmdbConfiguration.Images(
                    url = "somewhere",
                    sizes = listOf("fixed"),
                ),
        )

    private fun testStoredPaths() =
        ImagePaths(
            "https://image.tmdb.org/t/p/",
            "w154",
            "w500",
        )

    @Test
    fun `when paths are missing fetch and store them`() =
        runTest {
            // Given
            every { appPreferences.imagePaths() } returns flowOf(null)
            every { appPreferences.lastCheckedDateMillis(any()) } returns flowOf(System.currentTimeMillis())
            initializeSut()

            // When
            configSynchronizer.checkNow()

            // Then
            coVerify {
                appPreferences.updateLastCheckedDateMillis(any())
                appPreferences.updateImagePaths(any())
            }
        }

    @Test
    fun `when paths are old fetch and store them as an update`() =
        runTest {
            // Given
            every { appPreferences.lastCheckedDateMillis(any()) } returns flowOf(0L)
            initializeSut()

            // When
            configSynchronizer.checkNow()

            // Then
            coVerify {
                appPreferences.updateLastCheckedDateMillis(any())
                appPreferences.updateImagePaths(any())
            }
        }

    @Test
    fun `when paths are up to date do not fetch`() =
        runTest {
            clearMocks(appPreferences)
            // Given
            every { appPreferences.lastCheckedDateMillis(any()) } returns flowOf(System.currentTimeMillis())
            initializeSut()

            // When
            configSynchronizer.checkNow()

            // Then
            coVerify {
                appPreferences.updateLastCheckedDateMillis(any())
                appPreferences.updateImagePaths(any())
            }
        }

    @Test
    fun `when connection is down do not fetch`() =
        runTest {
            // Given
            clearMocks(appPreferences)
            every { appPreferences.lastCheckedDateMillis(any()) } returns flowOf(0L)
            every { connectivityTracker.isOnline() } returns flowOf(false)
            initializeSut()

            // When

            // Then
            coVerify(exactly = 0) { appPreferences.updateImagePaths(any()) }
        }

    @Test
    fun `when connection becomes enabled fetch`() =
        runTest {
            // Given
            clearMocks(appPreferences)
            every { appPreferences.lastCheckedDateMillis(any()) } returns flowOf(0L)
            val connection = MutableStateFlow(false)
            every { connectivityTracker.isOnline() } returns connection
            initializeSut()
            coVerify(exactly = 0) { appPreferences.updateImagePaths(any()) }

            // When
            connection.value = true

            // Then
            coVerify(exactly = 1) { appPreferences.updateImagePaths(any()) }
        }

    @Test
    fun `if genres are missing, fetch genres from api and update the internal genre db`() =
        runTest {
            // Given
            clearMocks(tmdbApi, genreRepository)
            every { appPreferences.lastCheckedDateMillis(any()) } returns flowOf(System.currentTimeMillis())
            coEvery { genreRepository.isEmpty() } returns true

            // When
            initializeSut()

            // Then
            coVerify {
                tmdbApi.getGenres()
                genreRepository.appendGenres(any())
            }
        }
}
