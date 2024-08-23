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
import com.lairofpixies.whatmovienext.models.network.TestOmdbApi
import com.lairofpixies.whatmovienext.models.network.TestTmdbApi
import com.lairofpixies.whatmovienext.models.network.TestWikidataApi
import com.lairofpixies.whatmovienext.models.network.TmdbApi
import com.lairofpixies.whatmovienext.models.network.WikidataApi
import com.lairofpixies.whatmovienext.models.preferences.AppPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ApiModule::class],
)
object TestApiModule {
    @Provides
    fun provideApiRepository(
        tmdbApi: TestTmdbApi,
        omdbApi: TestOmdbApi,
        wikidataApi: TestWikidataApi,
        remoteMapper: RemoteMapper,
    ): ApiRepository =
        ApiRepositoryImpl(
            tmdbApi,
            omdbApi,
            wikidataApi,
            remoteMapper,
            Dispatchers.IO,
        )

    @Singleton
    @Provides
    fun provideTestTmdbApi() = TestTmdbApi()

    @Singleton
    @Provides
    fun provideTestOmdbApi() = TestOmdbApi()

    @Singleton
    @Provides
    fun provideTestWikidataApi() = TestWikidataApi()

    @Provides
    @Singleton
    fun provideLocalhostCertificate(): HeldCertificate =
        HeldCertificate
            .Builder()
            .addSubjectAlternativeName("localhost")
            .build()

    @Provides
    @Singleton
    fun provideMockWebServer(heldCertificate: HeldCertificate): MockWebServer {
        val serverCertificates: HandshakeCertificates =
            HandshakeCertificates
                .Builder()
                .heldCertificate(heldCertificate)
                .build()

        return MockWebServer().apply {
            useHttps(serverCertificates.sslSocketFactory(), false)
            start()
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(heldCertificate: HeldCertificate): OkHttpClient {
        val clientCertificates =
            HandshakeCertificates
                .Builder()
                .addTrustedCertificate(heldCertificate.certificate)
                .build()

        return OkHttpClient
            .Builder()
            .sslSocketFactory(
                clientCertificates.sslSocketFactory(),
                clientCertificates.trustManager,
            ).build()
    }

    @Provides
    @Singleton
    fun provideTmdbApi(
        mockWebServer: MockWebServer,
        okHttpClient: OkHttpClient,
    ): TmdbApi {
        val tmdbOkHttpClient =
            okHttpClient
                .newBuilder()
                .addInterceptor(RequestInterceptorFactory.tmdbInterceptor())
                .build()

        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit
            .Builder()
            .client(tmdbOkHttpClient)
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOmdbApi(
        mockWebServer: MockWebServer,
        okHttpClient: OkHttpClient,
    ): OmdbApi {
        val omdbOkHttpClient =
            okHttpClient
                .newBuilder()
                .addInterceptor(RequestInterceptorFactory.omdbInterceptor())
                .build()

        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit
            .Builder()
            .client(omdbOkHttpClient)
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWikidataApi(
        mockWebServer: MockWebServer,
        okHttpClient: OkHttpClient,
    ): WikidataApi {
        val wikidataOkHttpClient =
            okHttpClient
                .newBuilder()
                .addInterceptor(RequestInterceptorFactory.wikidataInterceptor())
                .build()

        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit
            .Builder()
            .client(wikidataOkHttpClient)
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WikidataApi::class.java)
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
        tmdbApi: TestTmdbApi,
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
