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

**Database**

 * [Room](https://developer.android.com/jetpack/androidx/releases/room)

**Logging**

 * [Timber](https://github.com/JakeWharton/timber)

**Linting**

 * [Kotlinter](https://github.com/jeremymailen/kotlinter-gradle)
 * [KtLint](https://pinterest.github.io/ktlint/latest/)

**API**
 
 * [TMDB](https://www.themoviedb.org/)
 * [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin)
 
Skipped languages / frameworks
------------------------------

 * **Java**: traditionally Android apps were written in Java. Modern apps are written in **Kotlin**.
 * **Espresso**: this UI testing framework is for UIs based on Android Layouts. Since here the UI is written in Jetpack Compose, I am using its equivalent.
 * **EventBus**, **RxJava**, **RxKotlin**, **LiveData**: used **Kotlin Coroutines** and **Flows** instead, as the more modern alternative for asynchronous data.
 * **Koin**: used **Hilt** on top of **Dagger**, as recommended by Google.
 * **Mockito**: used **Mockk** instead, but I don't have any special preference. 
 * **Detekt**: relying on **Ktlint** for linting.


Feature tests
-------------

The project is developed using a BDD approach (Behavior Driven Development) with the **Cucumber** framework. 

 1. A new **Feature** file is added to *app/src/androidTest/assets/features*.
 2. Different scenarios related to the feature are added in the **Gherkin** language.
 3. **Step** definitions are added or adjusted as needed, and then the code that passes the test.
 4. In this project these Gherkin Scenarios are called "Feature tests", to distinguish them from the regular connected tests.
 5. The feature tests are run via the **Gradle Cucumber Runner Plugin** (also automatically on CI). 
 6. However, the plugin cannot run alongside the regular JUnit-based connected tests, it's either-or. As a workaround, there are two flavors defined for the project, the **"Cucumber"** flavor and the **"Junit"** flavor. The production code is the same, the only difference is, when running the connected tests, which runner and therefore which set of tests will be run: the feature tests or the connected tests.
