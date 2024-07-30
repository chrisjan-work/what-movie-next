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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lairofpixies.whatmovienext.models.data.ImagePaths
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppPreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : AppPreferences {
    override fun lastCheckedDateMillis(default: Long): Flow<Long> =
        dataStore.data
            .map { preferences ->
                val stored = preferences[KEY_LAST_CHECKED_DATE]
                try {
                    stored?.toLong() ?: default
                } catch (_: NumberFormatException) {
                    default
                }
            }

    override suspend fun updateLastCheckedDateMillis(dateMillis: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_CHECKED_DATE] = dateMillis.toString()
        }
    }

    override fun imagePaths(): Flow<ImagePaths?> =
        dataStore.data
            .map { preferences ->
                ImagePaths(
                    baseUrl = preferences[KEY_IMAGES_BASE_URL] ?: return@map null,
                    thumbnailPath = preferences[KEY_IMAGES_SMALL_OPTION] ?: return@map null,
                    coverPath = preferences[KEY_IMAGES_BIG_OPTION] ?: return@map null,
                )
            }

    override suspend fun updateImagePaths(config: ImagePaths) {
        dataStore.edit { preferences ->
            preferences[KEY_IMAGES_BASE_URL] = config.baseUrl
            preferences[KEY_IMAGES_SMALL_OPTION] = config.thumbnailPath
            preferences[KEY_IMAGES_BIG_OPTION] = config.coverPath
        }
    }

    companion object {
        val KEY_LAST_CHECKED_DATE = stringPreferencesKey("last_checked_date")
        val KEY_IMAGES_BASE_URL = stringPreferencesKey("images_base_url")
        val KEY_IMAGES_SMALL_OPTION = stringPreferencesKey("images_small_option")
        val KEY_IMAGES_BIG_OPTION = stringPreferencesKey("images_big_option")
    }
}
