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
import com.lairofpixies.whatmovienext.models.datastore.AppPreferences
import com.lairofpixies.whatmovienext.util.toCanonicalUrl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BackendConfigRepositoryImpl(
    private val appPreferences: AppPreferences,
//    private val movieApi: MovieApi,
    private val cacheExpirationTimeMillis: Long,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BackendConfigRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private var imagePaths: ImagePaths = ImagePaths("", "", "")

    override fun initializeConfiguration() {
        repositoryScope.launch {
            appPreferences.imagePaths().collect { storedPaths ->
                storedPaths?.let { imagePaths = storedPaths }
            }
        }

        checkNow()
    }

    override fun checkNow() {
        repositoryScope.launch {
            val lastImagePaths = appPreferences.imagePaths().firstOrNull()
            val lastCheckDateMillis = appPreferences.lastCheckedDateMillis(0L).firstOrNull() ?: 0L
            val dateThreshold = System.currentTimeMillis() - cacheExpirationTimeMillis
            if (lastImagePaths == null || lastCheckDateMillis < dateThreshold) {
                updatePaths()
            }
        }
    }

    private suspend fun updatePaths(): ImagePaths {
        val fetched =
            ImagePaths(
                baseUrl = "https://image.tmdb.org/t/p/",
                thumbnailPath = "w154",
                coverPath = "w500",
            )
        appPreferences.updateLastCheckedDateMillis(System.currentTimeMillis())
        appPreferences.updateImagePaths(fetched)
        return fetched
    }

    override fun getThumbnailUrl(posterPath: String?): String =
        if (!posterPath.isNullOrBlank()) {
            "${imagePaths.baseUrl}/${imagePaths.thumbnailPath}/$posterPath".toCanonicalUrl()
        } else {
            ""
        }

    override fun getCoverUrl(posterPath: String?): String =
        if (!posterPath.isNullOrBlank()) {
            "${imagePaths.baseUrl}/${imagePaths.coverPath}/$posterPath".toCanonicalUrl()
        } else {
            ""
        }
}
