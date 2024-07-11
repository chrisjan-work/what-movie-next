package com.lairofpixies.whatmovienext.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val watchState: WatchState = WatchState.PENDING,
    val isArchived: Boolean = false,
)

fun Movie.hasSaveableChanges(lastSavedMovie: Movie?): Boolean =
    when {
        title.isBlank() -> false
        lastSavedMovie == null -> true
        else -> title != lastSavedMovie.title
    }

fun Movie.hasQuietSaveableChanges(lastSavedMovie: Movie?): Boolean =
    when {
        title.isBlank() -> false
        lastSavedMovie == null -> true
        else -> watchState != lastSavedMovie.watchState
    }
