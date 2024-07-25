package com.lairofpixies.whatmovienext.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.lairofpixies.whatmovienext.models.data.AsyncMovieInfo
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.isMissing
import com.lairofpixies.whatmovienext.models.data.toList
import com.lairofpixies.whatmovienext.viewmodels.ArchiveViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes

@Composable
fun ArchiveScreen(archiveViewModel: ArchiveViewModel) {
    val archivedMovies: AsyncMovieInfo = archiveViewModel.archivedMovies.collectAsState().value
    val selection = archiveViewModel.selection.collectAsState().value

    if (archivedMovies.isMissing()) {
        archiveViewModel.onCancelAction()
    }

    Scaffold(
        modifier = Modifier.testTag(UiTags.Screens.ARCHIVE),
        bottomBar = {
            CustomBottomBar(
                items =
                    bottomItemsForArchive(
                        selection = selection,
                        onNavigateToMovieList = { archiveViewModel.onNavigateTo(Routes.AllMoviesView) },
                        onRestoreSelectedMovies = { archiveViewModel.restoreSelectedMovies() },
                        onDeleteSelectedMovies = { archiveViewModel.deleteSelectedMovies() },
                    ),
            )
        },
    ) { innerPadding ->
        Archive(
            archivedMovies = archivedMovies.toList(),
            selection = selection,
            append = { archiveViewModel.select(it) },
            remove = { archiveViewModel.deselect(it) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Composable
fun Archive(
    archivedMovies: List<Movie>,
    selection: Set<Movie>,
    append: (Movie) -> Unit,
    remove: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        LazyColumn {
            items(archivedMovies) { movie ->
                ArchivedMovieListItem(
                    movie = movie,
                    isSelected = movie in selection,
                ) { isSelected ->
                    if (isSelected) {
                        append(movie)
                    } else {
                        remove(movie)
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
    val selectedColor =
        if (isSelected) {
            Color.DarkGray
        } else {
            Color.Black
        }

    val negativeSelectedColor =
        if (isSelected) {
            Color.Gray
        } else {
            Color.White
        }

    Text(
        modifier =
            Modifier
                .background(negativeSelectedColor)
                .clickable {
                    onSelectionChanged(!isSelected)
                },
        color = selectedColor,
        text = movie.title,
    )
}

fun bottomItemsForArchive(
    selection: Set<Movie>,
    onNavigateToMovieList: () -> Unit,
    onRestoreSelectedMovies: () -> Unit,
    onDeleteSelectedMovies: () -> Unit,
): List<CustomBarItem> {
    val actionList =
        mutableListOf(
            CustomBarItem(ButtonSpec.MoviesShortcut, onNavigateToMovieList),
        )
    if (selection.isNotEmpty()) {
        actionList.addAll(
            listOf(
                CustomBarItem(ButtonSpec.RestoreAction, onRestoreSelectedMovies),
                CustomBarItem(ButtonSpec.DeleteAction, onDeleteSelectedMovies),
            ),
        )
    }
    return actionList
}
