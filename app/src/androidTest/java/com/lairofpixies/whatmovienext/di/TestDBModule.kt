package com.lairofpixies.whatmovienext.di

import android.content.Context
import androidx.room.Room
import com.lairofpixies.whatmovienext.database.InternalDatabase
import com.lairofpixies.whatmovienext.database.MovieDao
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.MovieRepositoryImpl
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
    ): InternalDatabase = Room.inMemoryDatabaseBuilder(context, InternalDatabase::class.java).build()

    @Singleton
    @Provides
    fun provideMovieDao(db: InternalDatabase): MovieDao = db.movieDao()
}
