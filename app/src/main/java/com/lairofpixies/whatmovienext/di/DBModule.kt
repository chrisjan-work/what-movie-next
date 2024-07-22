package com.lairofpixies.whatmovienext.di

import android.content.Context
import androidx.room.Room
import com.lairofpixies.whatmovienext.models.database.MovieDao
import com.lairofpixies.whatmovienext.models.database.MovieDatabase
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.database.MovieRepositoryImpl
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
            MovieDatabase::class.java,
            "what_movie_next_database",
        ).build()

    @Singleton
    @Provides
    fun provideMovieDao(db: MovieDatabase) = db.movieDao()
}
