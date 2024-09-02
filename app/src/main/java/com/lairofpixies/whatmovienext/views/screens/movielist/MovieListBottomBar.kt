/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.lairofpixies.whatmovienext.views.screens.movielist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar
import com.lairofpixies.whatmovienext.views.navigation.Routes
import com.lairofpixies.whatmovienext.views.screens.UiTags

@Composable
fun MovieListBottomBar(
    listViewModel: MovieListViewModel,
    modifier: Modifier = Modifier,
) {
    CustomBottomBar(
        items =
            bottomItemsForMovieList(
                isRouleteActive = listViewModel.canSpinRoulette(),
                onCreateNewMovie = {
                    listViewModel.onNavigateTo(Routes.CreateMovieView)
                },
                onQuickFindClicked = {
                    listViewModel.onOpenQuickFind()
                },
                onArrangeClicked = {
                    listViewModel.onOpenBottomMenu(null)
                },
                onRouletteClicked = {
                    listViewModel.onNavigateToRandomMovie()
                },
            ),
        modifier = modifier,
    )
}

fun bottomItemsForMovieList(
    isRouleteActive: Boolean,
    onCreateNewMovie: () -> Unit,
    onQuickFindClicked: () -> Unit,
    onArrangeClicked: () -> Unit,
    onRouletteClicked: () -> Unit,
): List<CustomBarItem> {
    val createItem = CustomBarItem(ButtonSpec.CreateMovieShortcut, onCreateNewMovie)

    val findItem = CustomBarItem(ButtonSpec.QuickFindAction, onQuickFindClicked)

    val rouletteItem =
        CustomBarItem(
            ButtonSpec.RouletteAction,
            tag = UiTags.Buttons.ROULETTE,
            enabled = isRouleteActive,
            onClick = onRouletteClicked,
        )

    val arrangeItem =
        CustomBarItem(
            ButtonSpec.ArrangeMenu,
            tag = UiTags.Buttons.ARRANGE_MENU,
            onClick = onArrangeClicked,
        )

    return listOfNotNull(
        arrangeItem,
        findItem,
        rouletteItem,
        createItem,
    )
}
