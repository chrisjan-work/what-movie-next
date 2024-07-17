package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.viewmodel.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomNavigationBar
import com.lairofpixies.whatmovienext.views.navigation.NavigationItem

object ArchiveTags {
    const val TAG_ARCHIVE_LIST = "ArchiveList"
}

@Composable
fun ArchiveScreen(
    archivedMovies: List<Movie>,
    viewModel: MainViewModel,
    navController: NavHostController,
) {
    val selection = remember { mutableStateOf(setOf<Movie>()) }

    val bottomBarItems =
        listOf(CustomBarItem(NavigationItem.MoviesShortcut)) +
            if (selection.value.isNotEmpty()) {
                listOf(
                    CustomBarItem(NavigationItem.RestoreAction) { TODO() },
                    CustomBarItem(NavigationItem.DeleteAction) { TODO() },
                )
            } else {
                emptyList()
            }

    Scaffold(
        bottomBar = {
            CustomNavigationBar(
                navController = navController,
                items = bottomBarItems,
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.testTag(ArchiveTags.TAG_ARCHIVE_LIST),
            ) {
                items(archivedMovies) { movie ->
                    ArchivedMovieListItem(
                        movie = movie,
                        isSelected = movie in selection.value,
                    ) { isSelected ->
                        if (isSelected) {
                            selection.value += movie
                        } else {
                            selection.value -= movie
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivedMovieListItem(
    movie: Movie,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
) {
    Text(
        modifier =
            Modifier.clickable {
                onSelectionChanged(!isSelected)
            },
        text = movie.title,
    )
}
