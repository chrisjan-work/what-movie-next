package com.lairofpixies.whatmovienext.models.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lairofpixies.whatmovienext.models.data.Movie

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
