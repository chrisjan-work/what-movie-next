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

import com.lairofpixies.whatmovienext.models.mappers.MovieMapper
import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepositoryImpl
import com.lairofpixies.whatmovienext.models.network.BackendConfigRepository
import com.lairofpixies.whatmovienext.models.network.BackendConfigRepositoryImpl
import com.lairofpixies.whatmovienext.models.network.MovieApi
import com.lairofpixies.whatmovienext.models.network.TestMovieApi
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
        movieApi: TestMovieApi,
        movieMapper: MovieMapper,
    ): ApiRepository = ApiRepositoryImpl(movieApi, movieMapper, Dispatchers.IO)

    @Singleton
    @Provides
    fun provideTestMovieApi() = TestMovieApi()

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
    fun provideMovieApi(
        mockWebServer: MockWebServer,
        okHttpClient: OkHttpClient,
    ): MovieApi {
        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit
            .Builder()
            .client(okHttpClient)
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MovieApi::class.java)
    }

    @Provides
    fun provideConfigRepo(): BackendConfigRepository = BackendConfigRepositoryImpl()
}
