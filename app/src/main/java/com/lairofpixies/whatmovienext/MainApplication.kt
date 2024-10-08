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
package com.lairofpixies.whatmovienext

import android.app.Application
import coil.ImageLoader
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.ConfigSynchronizer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var configRepository: ConfigRepository

    @Inject
    lateinit var configSynchronizer: ConfigSynchronizer

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        configRepository.trackConfiguration()
        configSynchronizer.syncConfig()
    }
}
