What Movie Next
===============

An Android app to keep track of movie recommendations.

CI status
---------

[![Tests](https://github.com/ChrisJan00/what-movie-next/actions/workflows/android.yml/badge.svg)](https://github.com/ChrisJan00/what-movie-next/actions/workflows/android.yml)

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
