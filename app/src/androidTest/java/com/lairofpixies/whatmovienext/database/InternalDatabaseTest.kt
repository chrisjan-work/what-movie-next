package com.lairofpixies.whatmovienext.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
        runTest {
            // Given an empty database
            assert(dao.getAllMovies().first().isEmpty())

            // When we insert a movie
            val movie =
                Movie(
                    id = 10,
                    title = "Casino",
                    watchState = WatchState.PENDING,
                )
            dao.insertMovie(movie)

            // Then the movie is in the database
            assertEquals(movie, dao.getMovie(10).first())
        }

    @Test
    fun `create and read several entries at a time`() =
        runTest {
            // Given an empty database
            assert(dao.getAllMovies().first().isEmpty())

            // When we insert some movies
            val movies =
                listOf(
                    Movie(
                        id = 1,
                        title = "Someone flew over the cuckoo's nest",
                        watchState = WatchState.PENDING,
                    ),
                    Movie(
                        id = 2,
                        title = "Watchmen",
                        watchState = WatchState.PENDING,
                    ),
                    Movie(
                        id = 3,
                        title = "A Beautiful Mind",
                        watchState = WatchState.PENDING,
                    ),
                )

            dao.insertMovies(movies)

            // Then the movie is in the database
            assertEquals(movies, dao.getAllMovies().first())
        }

    @Test
    fun `create and delete entry`() =
        runTest {
            // Given a database with a single movie
            assert(dao.getAllMovies().first().isEmpty())
            val movie = Movie(id = 1, title = "The Wizard of Oz", watchState = WatchState.PENDING)
            dao.insertMovie(movie)

            // When we remove it
            dao.delete(movie)

            // Then the movie is in the database
            assertEquals(emptyList<Movie>(), dao.getAllMovies().first())
        }

    @Test
    fun `update movie details`() =
        runTest {
            // Given a database with a single movie
            val movie = Movie(id = 9, title = "Stargate", watchState = WatchState.PENDING)
            dao.insertMovie(movie)

            // When updating the movie details
            dao.updateMovieDetails(movie.copy(title = "Stargate: Atlantis"))

            // Then the movie details are updated
            assertEquals(
                "Stargate: Atlantis",
                dao
                    .getMovie(9)
                    .first()
                    ?.title,
            )
        }

    @Test
    fun `set movie watch state`() =
        runTest {
            // Given a database with a single movie
            assert(dao.getAllMovies().first().isEmpty())
            val movie = Movie(id = 1, title = "The Wizard of Oz", watchState = WatchState.PENDING)
            dao.insertMovie(movie)

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
        runTest {
            // Given a database with a single movie
            assert(dao.getAllMovies().first().isEmpty())
            val movie =
                Movie(
                    id = 1,
                    title = "The Wizard of Oz",
                    watchState = WatchState.PENDING,
                    isArchived = false,
                )
            dao.insertMovie(movie)

            // When setting the movie to archived
            dao.archive(movie.id)

            // Then the movie is removed from the view list
            assert(dao.getAllMovies().first().isEmpty())
        }

    @Test
    fun `fetch single movie by id`() =
        runTest {
            // Given a database with a single movie
            val movie = Movie(id = 11, title = "The Searchers")
            dao.insertMovie(movie)

            // When fetching the movie by id
            val shouldBeMovie = dao.fetchMovieById(11)
            val shouldBeNull = dao.fetchMovieById(12)

            // Then the movie is returned or not
            assertEquals(movie, shouldBeMovie)
            assertEquals(null, shouldBeNull)
        }

    @Test
    fun `fetch movies by title`() =
        runTest {
            // Given a database with three movies, but one has a duplicated title
            val movies =
                listOf(
                    Movie(id = 1, title = "The Godfather"),
                    Movie(id = 2, title = "The Godfather II"),
                    Movie(id = 3, title = "The Godfather II"),
                )
            dao.insertMovies(movies)

            // When fetching the movies by title
            val duplicated = dao.fetchMoviesByTitle("The Godfather II")
            val single = dao.fetchMoviesByTitle("The Godfather")
            val none = dao.fetchMoviesByTitle("The Godfather III")

            // Then the movies are returned
            assertEquals(2, duplicated.size)
            assertEquals(1, single.size)
            assertEquals(0, none.size)
        }

    @Test
    fun `fetch movies by title is case insensitive`() =
        runTest {
            // Given a database with a single movie
            val movie = Movie(id = 1, title = "AbCd")
            dao.insertMovie(movie)

            // When fetching the movies by title with different case
            val lowerCase = dao.fetchMoviesByTitle("abcd")
            val upperCase = dao.fetchMoviesByTitle("ABCD")
            val none = dao.fetchMoviesByTitle("abcd123")

            // Then the movies are returned
            assertEquals(movie, lowerCase.first())
            assertEquals(movie, upperCase.first())
            assertEquals(emptyList<Movie>(), none)
        }
}
