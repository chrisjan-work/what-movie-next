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
import com.lairofpixies.whatmovienext.models.datastore.AppPreferences
import com.lairofpixies.whatmovienext.models.mappers.MovieMapper
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepositoryImpl
import com.lairofpixies.whatmovienext.models.network.BackendConfigRepository
import com.lairofpixies.whatmovienext.models.network.BackendConfigRepositoryImpl
import com.lairofpixies.whatmovienext.models.network.MovieApi
import com.lairofpixies.whatmovienext.models.network.RequestHeaderInterceptor
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
        movieApi: MovieApi,
        movieMapper: MovieMapper,
    ): ApiRepository =
        ApiRepositoryImpl(
            movieApi,
            movieMapper,
            Dispatchers.IO,
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(RequestHeaderInterceptor())
            .build()

    @Provides
    @Singleton
    fun provideMovieApi(okHttpClient: OkHttpClient): MovieApi {
        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.apiurl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(MovieApi::class.java)
    }

    @Provides
    @Singleton
    fun provideConfigRepository(appPreferences: AppPreferences): BackendConfigRepository =
        BackendConfigRepositoryImpl(
            appPreferences = appPreferences,
            cacheExpirationTimeMillis = BuildConfig.CACHE_EXPIRATION_TIME_MILLIS,
            ioDispatcher = Dispatchers.IO,
        )
}
