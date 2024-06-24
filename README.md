What Movie Next
===============

An Android app to keep track of movie recommendations.

Use case
--------

 At a party or social event, someone recommends you a movie.  
 You write the title into this app, as you would do in a notes app.  
 The app automatically fills up the details: director, genre, actors, year, rating, etc.  

 Some time later you are at home and want to watch something.  
 The app gives you a list of movies that you entered, which you can sort and filter however you want.  
 The app can choose one at random for you.  
 Once you've watched it, you can mark it as "watched" in the app.  


History
-------

Some years ago I started writing down a list of movies I want to watch some day. Classics, new ones, etc.  
At first, it was a plain text document in my laptop.  
Later, I started using a Google Docs spreadsheet, where I entered more details beyond title.  
Recently, I was trying to think of a simple app concept that I could implement in a few days, for the purpose of showcasing my skills as Android developer.  
I came up with the idea of implementing the movie list as a standalone app.  

Languages and Frameworks
------------------------

 * [Android SDK](https://developer.android.com/)
 * [Kotlin](https://kotlinlang.org/)
 * [Jetpack Compose](https://developer.android.com/develop/ui/compose) 
 * [Dagger/Hilt](https://dagger.dev/hilt/) 
 * [Espresso](https://developer.android.com/training/testing/espresso/)
 * [JUnit](https://junit.org/)
 * [Mockk](https://mockk.io/) 
 * [Retrofit](https://square.github.io/retrofit/)
 * [OkHttp](https://square.github.io/okhttp/)

Skipped languages / frameworks
------------------------------

I do have several years of experience with all the technologies in the list below, but opted for the alternatives in the list abover.

 * **Java**: traditionally Android apps were written in Java. Modern apps are written in **Kotlin**.
 * **EventBus**, **RxJava**, **LiveData**: used **Kotlin Coroutines** and **Flows** instead, as the more modern alternative for asynchronous data.
 * **Koin**: used **Hilt** on top of **Dagger**, as recommended by Google.
 * **Mockito**: used **Mockk** instead, but I don't have any special preference. 
