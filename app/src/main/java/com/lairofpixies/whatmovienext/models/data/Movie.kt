package com.lairofpixies.whatmovienext.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Long = NEW_ID,
    val title: String,
    val watchState: WatchState = WatchState.PENDING,
    val isArchived: Boolean = false,
) {
    companion object {
        const val NEW_ID = 0L
    }
}

fun Movie.hasSaveableChangesSince(lastSavedMovie: Movie?): Boolean =
    when {
        title.isBlank() -> false
        lastSavedMovie == null -> true
        else -> title != lastSavedMovie.title
    }

fun Movie.hasQuietSaveableChangesSince(lastSavedMovie: Movie?): Boolean =
    when {
        title.isBlank() -> false
        lastSavedMovie == null -> true
        else -> watchState != lastSavedMovie.watchState
    }

fun Movie.isNew(): Boolean = id == Movie.NEW_ID
