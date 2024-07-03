package com.lairofpixies.whatmovienext.viewmodel

import androidx.lifecycle.ViewModel
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        repo: MovieRepository,
    ) : ViewModel() {
        val movieList: StateFlow<List<Movie>> = repo.movies
    }
