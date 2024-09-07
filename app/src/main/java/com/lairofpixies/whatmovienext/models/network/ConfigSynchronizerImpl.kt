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
import com.lairofpixies.whatmovienext.models.network.data.TmdbConfiguration
import com.lairofpixies.whatmovienext.models.preferences.AppPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ConfigSynchronizerImpl(
    private val appPreferences: AppPreferences,
    private val tmdbApi: TmdbApi,
    private val connectivityTracker: ConnectivityTracker,
    private val cacheExpirationTimeMillis: Long,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ConfigSynchronizer {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun syncConfig() {
        repositoryScope.launch {
            connectivityTracker.isOnline().collect { isOnline ->
                if (isOnline) checkNow()
            }
        }
    }

    override fun checkNow() {
        suspend fun emptyImagePaths() = appPreferences.imagePaths().firstOrNull() == null

        fun dateThreshold() = System.currentTimeMillis() - cacheExpirationTimeMillis

        suspend fun lastCheckDateMillis() = appPreferences.lastCheckedDateMillis(0L).firstOrNull() ?: 0L

        repositoryScope.launch {
            val shouldUpdate =
                when {
                    emptyImagePaths() -> true
                    lastCheckDateMillis() < dateThreshold() -> true
                    else -> false
                }
            if (shouldUpdate) {
                updateConfig()
            }
        }
    }

    private suspend fun updateConfig() {
        parseConfiguration(tmdbApi.getConfiguration())?.let { fetched ->
            appPreferences.updateImagePaths(fetched)
        }
        appPreferences.updateLastCheckedDateMillis(System.currentTimeMillis())
    }

    private fun parseConfiguration(configuration: TmdbConfiguration?): ImagePaths? =
        configuration?.let {
            with(it.images) {
                val small: String
                val big: String
                when (posterSizes.size) {
                    0 -> return@let null
                    1 -> {
                        small = posterSizes.first()
                        big = posterSizes.first()
                    }

                    in 2..3 -> {
                        small = posterSizes.first()
                        big = posterSizes.last()
                    }

                    else -> {
                        small = posterSizes[1]
                        big = posterSizes[posterSizes.size - 2]
                    }
                }

                // take second in list, or fallback to first
                val profile = profileSizes.getOrNull(1) ?: profileSizes.firstOrNull() ?: ""

                ImagePaths(
                    baseUrl = url,
                    thumbnailPath = small,
                    coverPath = big,
                    facePath = profile,
                )
            }
        }
}
