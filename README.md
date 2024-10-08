What Movie Next
===============

An Android app to keep track of movie recommendations.

CI status
---------

[![Tests](https://github.com/ChrisJan00/what-movie-next/actions/workflows/android.yml/badge.svg)](https://github.com/ChrisJan00/what-movie-next/actions/workflows/android.yml)

Releases
--------

[Download APK](https://github.com/chrisjan-work/what-movie-next/releases/)


Use case
--------

 At a party or social event, someone recommends you a movie.  
 You write the title into this app, as you would do in a notes app.  
 The app automatically fills up the details: director, genre, actors, year, rating, etc.  

 Some time later you are at home and want to watch something.  
 The app gives you a list of movies that you entered, which you can sort and filter however you want.  
 The app can choose one at random for you.  
 Once you've watched it, you can mark it as "watched" in the app.  


Motivation
----------

Some years ago I started writing down a list of movies I want to watch some day. Classics, new ones, etc.  
At first, it was a plain text document in my laptop.  
Later, I started using a Google Docs spreadsheet, where I entered more details beyond title.  
Recently, I was trying to think of a simple app concept that I could implement in my free time, for the purpose of showcasing my skills as Android developer. As a professional with more than a decade of experience, all my code from these years is property of my clients, and I have nothing to show.
I came up with the idea of implementing the movie list as a standalone app.  

Languages and Frameworks
------------------------

**Basics**

 * [Android SDK](https://developer.android.com/)
 * [Kotlin](https://kotlinlang.org/)

**User Interface**

 * [Jetpack Compose](https://developer.android.com/develop/ui/compose) 
 * [Jetpack Navigation](https://developer.android.com/jetpack/androidx/releases/navigation)
 * [Material Design Icons](https://fonts.google.com/icons)

**Dependency Injection**

 * [Dagger/Hilt](https://dagger.dev/hilt/) 

**Building**

 * [Gradle](https://gradle.org/)
 * [Gradle Kotlin DSL](https://kotlinlang.org/docs/gradle.html)

**Testing**

 * [Jetpack Compose UI Testing framework](https://developer.android.com/develop/ui/compose/testing)
 * [JUnit](https://junit.org/)
 * [Mockk](https://mockk.io/) 

**BDD / Behavior Driven Development**
  
 * [Cucumber](https://cucumber.io/)
 * [Cucumber Android plugin](https://github.com/cucumber/cucumber-android)
  
**CI / Continuous Integration**

 * [Github Actions](https://docs.github.com/en/actions)

**RESTful APIs**

 * [Retrofit](https://square.github.io/retrofit/)
 * [OkHttp](https://square.github.io/okhttp/)
 * [Moshi](https://github.com/square/moshi)
 * [ConnectivityManager](https://developer.android.com/reference/android/net/ConnectivityManager)

**Database**

 * [Room](https://developer.android.com/jetpack/androidx/releases/room)
 * [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)

**Logging**

 * [Timber](https://github.com/JakeWharton/timber)

**Linting**

 * [Kotlinter](https://github.com/jeremymailen/kotlinter-gradle)
 * [KtLint](https://pinterest.github.io/ktlint/latest/)

**API**
 
 * [TMDB](https://www.themoviedb.org/)
 * [OMDB](https://www.omdbapi.com/)
 * [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin)

**Images**
 * [Coil](https://github.com/coil-kt/coil)

Data Sources
------------

 * Basic information about the movies and the TMDB logo comes from The Movie Database (https://www.themoviedb.org)
 * Information about the ratings comes from The Open Movie Database Api (https://www.omdbapi.com)
 * IMDB logo from iconduck https://iconduck.com/icons/14111/imdb (The asset is MIT-licensed)
 * RT logo from iconduck https://iconduck.com/icons/243754/rotten-tomatoes (MIT license)
 * Metacritic logo from Icons8 https://icons8.com/icons/set/metacritic
 * Extra information about ratings and links from Wikidata (https://wikidata.org)

Features
--------

 * Search for movies at The Movie Database (TMDB) and store them in the local database
 * Mark movies as seen / to watch
 * Remove/restore movies from the internal database
 * Quick find movies by text (title, genre, director)
 * Sort movies by: year, runtime, score, director, title, etc.
 * Filter movies by: year, runtime, score, director, etc.
 * Shuffle movies
 * Pick one random movie from the filtered list (Roulette)
 * Import/export whole list of saved movies as json
 * Automatic backup of all data
 * Translations: English, German, Spanish, Catalan
 * Movie details: poster, plot summary, cast and crew, etc.
 * Links to the movie in external pages: Tmdb, Imdb, Rotten Tomatoes, Metacritic
 * Rotten Tomatoes score and Metacritic score for every movie.
 * Share individual movies via deep linking.

Screenshots
-----------

* Movie List: General view, Quick find feature, Dropdown menu with options for import/export/view archived movies

  ![Movie List](screenshots/Movie_list.png)
  ![Find movie](screenshots/Quick_find_feature.png)
  ![Dropdown menu](screenshots/Dropdown_menu_view.png)

* Organize: Sort by score, filter by director and rt score, picker for genre filter

  ![Sort list](screenshots/Feature_sort.png)
  ![Filter by director](screenshots/Filter_by_director_and_score.png)
  ![Filter by genre](screenshots/Filter_by_genre.png)

* Search online: Start new search, search results screen

  ![Search title online](screenshots/Start_search.png)
  ![Search results](screenshots/Search_results.png)

* Details: Movie details, cast, links, share movie

  ![Movie Details](screenshots/Details_top.png)
  ![External Links](screenshots/Details_links.png)
  ![Share movie](screenshots/Feature_share.png)

* Translations: German, Catalan

  ![German Translation](screenshots/German_translation.png)
  ![Catalan Translation](screenshots/Catalan_translation.png)
