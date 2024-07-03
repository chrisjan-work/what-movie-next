package com.lairofpixies.whatmovienext.di

import android.content.Context
import androidx.room.Room
import com.lairofpixies.whatmovienext.database.InternalDatabase
import com.lairofpixies.whatmovienext.database.MovieDao
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.MovieRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DBModule {
    @Provides
    fun provideMovieRepository(movieDao: MovieDao): MovieRepository = MovieRepositoryImpl(movieDao)

    @Singleton
    @Provides
    fun provideInternalDatabase(
        @ApplicationContext app: Context,
    ) = Room
        .databaseBuilder(
            app,
            InternalDatabase::class.java,
            "what_movie_next_database",
        ).build()

    @Singleton
    @Provides
    fun provideMovieDao(db: InternalDatabase) = db.movieDao()
}
