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
import com.lairofpixies.whatmovienext.models.data.remote.RemoteConfiguration
import com.lairofpixies.whatmovienext.models.datastore.AppPreferences
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackendConfigRepositoryImplTest {
    private lateinit var appPreferences: AppPreferences
    private lateinit var movieApi: MovieApi
    private lateinit var connectivityTracker: ConnectivityTracker
    private lateinit var backendConfigRepository: BackendConfigRepository

    @Before
    fun setUp() {
        movieApi = mockk(relaxed = true)
        appPreferences = mockk(relaxed = true)
        connectivityTracker = mockk(relaxed = true)

        backendConfigRepository =
            BackendConfigRepositoryImpl(
                appPreferences = appPreferences,
                movieApi = movieApi,
                connectivityTracker = connectivityTracker,
                cacheExpirationTimeMillis = 1000L,
                ioDispatcher = UnconfinedTestDispatcher(),
            )
        // Feed valid paths by default
        coEvery { movieApi.getConfiguration() } returns testConfiguration()
        every { appPreferences.imagePaths() } returns flowOf(testStoredPaths())
        every { connectivityTracker.isOnline() } returns flowOf(true)
        backendConfigRepository.initializeConfiguration()
    }

    private fun testConfiguration() =
        RemoteConfiguration(
            images =
                RemoteConfiguration.ImagesConfiguration(
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
            // When
            backendConfigRepository.checkNow()
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

            // When
            backendConfigRepository.checkNow()
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

            // When
            backendConfigRepository.checkNow()
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

            // When
            backendConfigRepository.initializeConfiguration()

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

            backendConfigRepository.initializeConfiguration()
            coVerify(exactly = 0) { appPreferences.updateImagePaths(any()) }

            // When
            connection.value = true

            // Then
            coVerify(exactly = 1) { appPreferences.updateImagePaths(any()) }
        }

    @Test
    fun getThumbnailUrl() {
        val url = backendConfigRepository.getThumbnailUrl("/test")
        assertEquals("https://image.tmdb.org/t/p/w154/test", url)
    }

    @Test
    fun getCoverUrl() {
        val url = backendConfigRepository.getCoverUrl("/test")
        assertEquals("https://image.tmdb.org/t/p/w500/test", url)
    }
}
