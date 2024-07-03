package com.lairofpixies.whatmovienext.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class InternalDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
