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

import com.lairofpixies.whatmovienext.BuildConfig
import com.lairofpixies.whatmovienext.models.database.GenreRepository
import com.lairofpixies.whatmovienext.models.mappers.RemoteMapper
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepositoryImpl
import com.lairofpixies.whatmovienext.models.network.ConfigRepository
import com.lairofpixies.whatmovienext.models.network.ConfigRepositoryImpl
import com.lairofpixies.whatmovienext.models.network.ConfigSynchronizer
import com.lairofpixies.whatmovienext.models.network.ConfigSynchronizerImpl
import com.lairofpixies.whatmovienext.models.network.ConnectivityTracker
import com.lairofpixies.whatmovienext.models.network.OmdbApi
import com.lairofpixies.whatmovienext.models.network.RequestInterceptorFactory
import com.lairofpixies.whatmovienext.models.network.TmdbApi
import com.lairofpixies.whatmovienext.models.preferences.AppPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    fun provideApiRepository(
        tmdbApi: TmdbApi,
        remoteMapper: RemoteMapper,
    ): ApiRepository =
        ApiRepositoryImpl(
            tmdbApi,
            remoteMapper,
            Dispatchers.IO,
        )

    @Provides
    @Singleton
    fun provideTmdbApi(): TmdbApi {
        val interceptor = RequestInterceptorFactory.tmdbInterceptor()

        val okHttpClient: OkHttpClient =
            OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .build()

        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.tmdburl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(TmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOmdbApi(): OmdbApi {
        val interceptor = RequestInterceptorFactory.omdbInterceptor()

        val okHttpClient: OkHttpClient =
            OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .build()

        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.omdburl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(OmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideConfigRepository(appPreferences: AppPreferences): ConfigRepository =
        ConfigRepositoryImpl(
            appPreferences = appPreferences,
            ioDispatcher = Dispatchers.IO,
        )

    @Provides
    @Singleton
    fun provideConfigSynchronizer(
        appPreferences: AppPreferences,
        tmdbApi: TmdbApi,
        genreRepository: GenreRepository,
        remoteMapper: RemoteMapper,
        connectivityTracker: ConnectivityTracker,
    ): ConfigSynchronizer =
        ConfigSynchronizerImpl(
            appPreferences = appPreferences,
            tmdbApi = tmdbApi,
            genreRepository = genreRepository,
            remoteMapper = remoteMapper,
            connectivityTracker = connectivityTracker,
            cacheExpirationTimeMillis = BuildConfig.CACHE_EXPIRATION_TIME_MILLIS,
            ioDispatcher = Dispatchers.IO,
        )
}
