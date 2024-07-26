package com.lairofpixies.whatmovienext.di

import com.lairofpixies.whatmovienext.models.network.ApiRepository
import com.lairofpixies.whatmovienext.models.network.ApiRepositoryImpl
import com.lairofpixies.whatmovienext.models.network.FakeMovieApi
import com.lairofpixies.whatmovienext.models.network.MovieApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    fun provideApiRepository(movieApi: MovieApi): ApiRepository = ApiRepositoryImpl(movieApi, Dispatchers.IO)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .build()

    @Provides
    @Singleton
    fun provideMovieApi(okHttpClient: OkHttpClient): MovieApi {
        // TODO: uncomment and get rid of the fake one
//        val moshi =
//            Moshi
//                .Builder()
//                .add(KotlinJsonAdapterFactory())
//                .build()
//
//        return Retrofit
//            .Builder()
//            .baseUrl(BuildConfig.BASE_URL)
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .client(okHttpClient)
//            .build()
//            .create(MovieApi::class.java)
        return FakeMovieApi()
    }
}
