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
import com.lairofpixies.whatmovienext.util.toCanonicalUrl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ConfigRepositoryImpl(
    private val appPreferences: AppPreferences,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ConfigRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private var imagePaths: ImagePaths = ImagePaths("", "", "", "")

    override fun trackConfiguration() {
        repositoryScope.launch {
            appPreferences.imagePaths().collect { storedPaths ->
                storedPaths?.let { imagePaths = storedPaths }
            }
        }
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

    override fun getFaceUrl(profilePath: String?): String =
        if (!profilePath.isNullOrBlank()) {
            "${imagePaths.baseUrl}/${imagePaths.facePath}/$profilePath".toCanonicalUrl()
        } else {
            ""
        }
}
