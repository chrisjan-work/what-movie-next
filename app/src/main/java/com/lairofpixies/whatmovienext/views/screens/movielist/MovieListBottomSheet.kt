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

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.util.dpf
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.BottomMenuOption
import com.lairofpixies.whatmovienext.views.state.SortingSetup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListBottomSheet(
    listViewModel: MovieListViewModel,
    modifier: Modifier = Modifier,
) {
    val scaffoldState =
        rememberBottomSheetScaffoldState(
            bottomSheetState =
                rememberStandardBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    skipHiddenState = false,
                ),
        )

    // react to viewmodel menu requests
    LaunchedEffect(listViewModel.bottomMenuState) {
        listViewModel.bottomMenuState.collect { (_, isOpen) ->
            if (!isOpen && scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                scaffoldState.bottomSheetState.hide()
            } else if (isOpen && scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
                scaffoldState.bottomSheetState.expand()
            }
        }
    }

    // notify viewmodel if sheet has been closed
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }.collect {
            if (it != SheetValue.Expanded && listViewModel.bottomMenuState.value.isOpen) {
                listViewModel.closeBottomMenu()
            }
        }
    }

    // content
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetTabs(
                bottomMenuOption =
                    listViewModel.bottomMenuState
                        .collectAsState()
                        .value.bottomMenuOption,
                selectMenu = { listViewModel.onOpenBottomMenu(it) },
            )
            BottomSheetMenu(
                listViewModel,
            )
        },
        sheetPeekHeight = 0.dp,
        content = {},
        modifier = modifier,
    )
}

@Composable
fun BottomSheetTabs(
    bottomMenuOption: BottomMenuOption,
    selectMenu: (BottomMenuOption) -> Unit,
) {
    val tabHeight = 24.dpf
    TabRow(
        selectedTabIndex = bottomMenuOption.ordinal,
        modifier = Modifier.height(tabHeight),
    ) {
        BottomMenuTab(
            currentOption = bottomMenuOption,
            tabOption = BottomMenuOption.Sorting,
            select = { selectMenu(BottomMenuOption.Sorting) },
            buttonSpec = ButtonSpec.SortingMenu,
            height = tabHeight,
            modifier = Modifier.testTag(UiTags.Buttons.SORT_TAB),
        )
        BottomMenuTab(
            currentOption = bottomMenuOption,
            tabOption = BottomMenuOption.Filtering,
            select = { selectMenu(BottomMenuOption.Filtering) },
            buttonSpec = ButtonSpec.FilterMenu,
            height = tabHeight,
            modifier = Modifier.testTag(UiTags.Buttons.FILTER_TAB),
        )
    }
}

@Composable
fun BottomMenuTab(
    currentOption: BottomMenuOption,
    tabOption: BottomMenuOption,
    select: () -> Unit,
    buttonSpec: ButtonSpec,
    modifier: Modifier = Modifier,
    height: Dp = 24.dpf,
) {
    val label = stringResource(buttonSpec.labelRes)
    LeadingIconTab(
        selected = currentOption == tabOption,
        onClick = select,
        icon = {
            Icon(
                buttonSpec.icon,
                contentDescription = label,
                modifier = Modifier.size(height / 2),
            )
        },
        text = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier.height(height),
    )
}

@Composable
fun BottomSheetMenu(listViewModel: MovieListViewModel) {
    val bottomMenuOption =
        listViewModel.bottomMenuState
            .collectAsState()
            .value.bottomMenuOption
    val currentPreset = listViewModel.currentPreset.collectAsState().value

    when (bottomMenuOption) {
        BottomMenuOption.Sorting ->
            SortingMenu(
                sortingSetup = currentPreset.sortingSetup,
                onSelectAction = { criteria, direction ->
                    listViewModel.updateSortingSetup(SortingSetup(criteria, direction))
                },
            )

        BottomMenuOption.Filtering -> {
            FilteringMenu(
                listFilters = currentPreset.listFilters,
                onListFiltersChanged = { listViewModel.setListFilters(it) },
                presetMapper = listViewModel.presetMapper(),
                allGenres = listViewModel.allGenreNamesMap(),
                allDirectors = listViewModel.allDirectors.collectAsState().value,
                showPopup = { listViewModel.showPopup(it) },
            )
        }
    }
}
