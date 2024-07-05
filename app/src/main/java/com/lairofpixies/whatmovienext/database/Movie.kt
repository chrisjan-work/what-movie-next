package com.lairofpixies.whatmovienext.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val watchState: WatchState = WatchState.PENDING,
    val isArchived: Boolean = false,
)
