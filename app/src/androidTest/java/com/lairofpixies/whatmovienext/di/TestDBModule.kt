package com.lairofpixies.whatmovienext.di

import android.content.Context
import androidx.room.Room
import com.lairofpixies.whatmovienext.models.database.MovieDao
import com.lairofpixies.whatmovienext.models.database.MovieDatabase
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.database.MovieRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DBModule::class],
)
object TestDBModule {
    @Provides
    fun provideMovieRepository(movieDao: MovieDao): MovieRepository = MovieRepositoryImpl(movieDao)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): MovieDatabase = Room.inMemoryDatabaseBuilder(context, MovieDatabase::class.java).build()

    @Singleton
    @Provides
    fun provideMovieDao(db: MovieDatabase): MovieDao = db.movieDao()
}
