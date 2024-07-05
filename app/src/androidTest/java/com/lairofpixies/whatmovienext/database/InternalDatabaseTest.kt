package com.lairofpixies.whatmovienext.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InternalDatabaseTest {
    private lateinit var db: InternalDatabase
    private lateinit var dao: MovieDao

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(appContext, InternalDatabase::class.java).build()
        dao = db.movieDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `create and read entry`() =
        runBlocking {
            // Given an empty database
            assert(dao.getAllMovies().first().isEmpty())

            // When we insert a movie
            val movie =
                Movie(
                    id = 1,
                    title = "Someone flew over the cuckoo's nest",
                    watchState = WatchState.PENDING,
                )
            dao.insertMovies(listOf(movie))

            // Then the movie is in the database
            assertEquals(listOf(movie), dao.getAllMovies().first())
        }

    @Test
    fun `create and delete entry`() =
        runBlocking {
            // Given a database with a single movie
            assert(dao.getAllMovies().first().isEmpty())
            val movie = Movie(id = 1, title = "The Wizard of Oz", watchState = WatchState.PENDING)
            dao.insertMovies(listOf(movie))

            // When we remove it
            dao.delete(movie)

            // Then the movie is in the database
            assertEquals(emptyList<Movie>(), dao.getAllMovies().first())
        }

    @Test
    fun `set movie watch state`() =
        runBlocking {
            // Given a database with a single movie
            assert(dao.getAllMovies().first().isEmpty())
            val movie = Movie(id = 1, title = "The Wizard of Oz", watchState = WatchState.PENDING)
            dao.insertMovies(listOf(movie))

            // When setting the movie to watched
            dao.updateWatchState(movie.id, WatchState.WATCHED)

            // Then the movie is watched
            assertEquals(
                WatchState.WATCHED,
                dao
                    .getMovie(1)
                    .first()
                    ?.watchState,
            )
        }

    @Test
    fun `archive movie`() =
        runBlocking {
            // Given a database with a single movie
            assert(dao.getAllMovies().first().isEmpty())
            val movie =
                Movie(
                    id = 1,
                    title = "The Wizard of Oz",
                    watchState = WatchState.PENDING,
                    isArchived = false,
                )
            dao.insertMovies(listOf(movie))

            // When setting the movie to archived
            dao.archive(movie.id)

            // Then the movie is removed from the view list
            assert(dao.getAllMovies().first().isEmpty())
        }
}
