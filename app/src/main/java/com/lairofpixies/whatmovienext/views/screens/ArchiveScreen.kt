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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.viewmodels.MainViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.state.ErrorState

@Composable
fun ArchiveScreen(
    archivedMovies: List<Movie>,
    onCancelAction: () -> Unit,
    viewModel: MainViewModel,
    navController: NavHostController,
) {
    val selection = remember { mutableStateOf(setOf<Movie>()) }

    if (archivedMovies.isEmpty()) {
        onCancelAction()
    }

    Scaffold(
        modifier = Modifier.testTag(UiTags.Screens.ARCHIVE),
        bottomBar = {
            CustomBottomBar(
                items =
                    bottomItemsForArchive(
                        selection,
                        viewModel,
                        navController,
                    ),
            )
        },
    ) { innerPadding ->
        Archive(
            archivedMovies = archivedMovies,
            selection = selection,
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
    selection: MutableState<Set<Movie>>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        LazyColumn {
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
    selection: MutableState<Set<Movie>>,
    viewModel: MainViewModel,
    navController: NavHostController,
): List<CustomBarItem> {
    val actionList =
        mutableListOf(
            CustomBarItem(ButtonSpec.MoviesShortcut) {
                navController.navigate(Routes.AllMoviesView.route)
            },
        )

    if (selection.value.isNotEmpty()) {
        actionList.add(
            CustomBarItem(ButtonSpec.RestoreAction) {
                viewModel.restoreMovies(selection.value.toList())
            },
        )
        actionList.add(
            CustomBarItem(ButtonSpec.DeleteAction) {
                viewModel.showError(
                    ErrorState.ConfirmDeletion {
                        viewModel.deleteMovies(selection.value.toList())
                        selection.value = emptySet()
                    },
                )
            },
        )
    }

    return actionList
}
