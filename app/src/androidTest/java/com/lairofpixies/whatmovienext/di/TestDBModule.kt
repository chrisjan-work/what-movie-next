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
package com.lairofpixies.whatmovienext.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.lairofpixies.whatmovienext.models.database.MovieDao
import com.lairofpixies.whatmovienext.models.database.MovieDatabase
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.database.MovieRepositoryImpl
import com.lairofpixies.whatmovienext.models.database.PresetDao
import com.lairofpixies.whatmovienext.models.database.PresetRepository
import com.lairofpixies.whatmovienext.models.database.PresetRepositoryImpl
import com.lairofpixies.whatmovienext.models.mappers.DbMapper
import com.lairofpixies.whatmovienext.models.mappers.PresetMapper
import com.lairofpixies.whatmovienext.models.preferences.AppPreferences
import com.lairofpixies.whatmovienext.models.preferences.AppPreferencesImpl
import com.lairofpixies.whatmovienext.models.preferences.TestDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DBModule::class],
)
object TestDBModule {
    @Provides
    fun provideMovieRepository(
        movieDao: MovieDao,
        dbMapper: DbMapper,
    ): MovieRepository = MovieRepositoryImpl(movieDao, dbMapper, ioDispatcher = Dispatchers.IO)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): MovieDatabase = Room.inMemoryDatabaseBuilder(context, MovieDatabase::class.java).build()

    @Singleton
    @Provides
    fun provideMovieDao(db: MovieDatabase): MovieDao = db.movieDao()

    @Singleton
    @Provides
    fun providePresetDao(db: MovieDatabase): PresetDao = db.presetDao()

    @Singleton
    @Provides
    fun provideDataStore(): DataStore<Preferences> = TestDataStore()

    @Provides
    fun provideAppPreferences(dataStore: DataStore<Preferences>): AppPreferences = AppPreferencesImpl(dataStore)

    @Provides
    fun providePresetRepository(
        presetDao: PresetDao,
        presetMapper: PresetMapper,
    ): PresetRepository =
        PresetRepositoryImpl(
            presetDao,
            presetMapper,
            Dispatchers.IO,
        )
}
