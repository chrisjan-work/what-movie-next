package com.lairofpixies.whatmovienext.di

import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepositoryImpl
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
    fun provideApiRepository(movieApi: TestMovieApi): ApiRepository = ApiRepositoryImpl(movieApi, Dispatchers.IO)

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
}
