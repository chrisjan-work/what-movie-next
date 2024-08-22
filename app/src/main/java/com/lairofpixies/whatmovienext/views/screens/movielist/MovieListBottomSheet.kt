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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.mappers.PresetMapper
import com.lairofpixies.whatmovienext.util.printableRuntimePacked
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.BottomMenuOption
import com.lairofpixies.whatmovienext.views.state.BottomMenuState
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListBottomSheet(
    bottomMenuState: StateFlow<BottomMenuState>,
    selectMenu: (BottomMenuOption) -> Unit,
    sortingSetup: SortingSetup,
    updateSortingSetup: (SortingSetup) -> Unit,
    listFilters: ListFilters,
    onListFiltersChanged: (ListFilters) -> Unit,
    presetMapper: PresetMapper,
    showPopup: (PopupInfo) -> Unit,
    closeBottomMenu: () -> Unit,
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
    LaunchedEffect(bottomMenuState) {
        bottomMenuState.collect { (_, isOpen) ->
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
            if (it != SheetValue.Expanded && bottomMenuState.value.isOpen) {
                closeBottomMenu()
            }
        }
    }

    // content
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetTabs(
                bottomMenuOption = bottomMenuState.collectAsState().value.bottomMenuOption,
                selectMenu = selectMenu,
            )
            BottomSheetMenu(
                bottomMenuOption = bottomMenuState.collectAsState().value.bottomMenuOption,
                sortingSetup = sortingSetup,
                updateSortingSetup = updateSortingSetup,
                listFilters = listFilters,
                onListFiltersChanged = onListFiltersChanged,
                presetMapper = presetMapper,
                showPopup = showPopup,
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
    val tabHeight = 24.dp
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
    height: Dp = 24.dp,
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
fun BottomSheetMenu(
    bottomMenuOption: BottomMenuOption,
    sortingSetup: SortingSetup,
    updateSortingSetup: (SortingSetup) -> Unit,
    listFilters: ListFilters,
    onListFiltersChanged: (ListFilters) -> Unit,
    presetMapper: PresetMapper,
    showPopup: (PopupInfo) -> Unit,
) {
    when (bottomMenuOption) {
        BottomMenuOption.Sorting ->
            SortingMenu(
                sortingSetup = sortingSetup,
                onSelectAction = { criteria, direction ->
                    updateSortingSetup(SortingSetup(criteria, direction))
                },
            )

        BottomMenuOption.Filtering ->
            FilteringMenu(
                listFilters = listFilters,
                onListFiltersChanged = onListFiltersChanged,
                presetMapper = presetMapper,
                showPopup = showPopup,
            )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortingMenu(
    sortingSetup: SortingSetup,
    onSelectAction: (SortingCriteria, SortingDirection) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 8.dp)
                .testTag(UiTags.Menus.SORTING),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SortingCriteria.entries
            .map { criteria ->
                SortingButton(
                    criteria = criteria,
                    direction = sortingSetup.direction,
                    isSelected = criteria == sortingSetup.criteria,
                    onSelectAction = onSelectAction,
                )
            }
    }
}

@Composable
fun SortingButton(
    criteria: SortingCriteria,
    direction: SortingDirection,
    isSelected: Boolean,
    onSelectAction: (SortingCriteria, SortingDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }
    Box(
        modifier =
            modifier
                .padding(3.dp)
                .size(width = 120.dp, height = 32.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).clickable {
                    if (isSelected) {
                        onSelectAction(criteria, direction.opposite())
                    } else {
                        onSelectAction(criteria, SortingDirection.Default)
                    }
                },
    ) {
        Text(
            text = stringResource(id = criteria.display),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            modifier =
                Modifier
                    .padding(8.dp)
                    .size(100.dp)
                    .align(Alignment.CenterStart),
        )
        // do not show arrow for random
        if (isSelected && criteria != SortingCriteria.Random) {
            Icon(
                if (direction == SortingDirection.Ascending) {
                    Icons.Outlined.ArrowUpward
                } else {
                    Icons.Outlined.ArrowDownward
                },
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .size(12.dp)
                        .align(Alignment.CenterEnd),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilteringMenu(
    listFilters: ListFilters,
    onListFiltersChanged: (ListFilters) -> Unit,
    presetMapper: PresetMapper,
    showPopup: (PopupInfo) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 8.dp)
                .testTag(UiTags.Menus.SORTING),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ListModeButton(
            listFilters.listMode,
            onListModeChanged = { listMode ->
                onListFiltersChanged(listFilters.copy(listMode = listMode))
            },
        )
        MinMaxButton(
            label = stringResource(R.string.by_runtime),
            filterValues = listFilters.runtime,
            range = PresetMapper.MIN_RUNTIME..PresetMapper.MAX_RUNTIME,
            onFilterValuesChanged = { minMaxFilter ->
                onListFiltersChanged(listFilters.copy(runtime = minMaxFilter))
            },
            valueToText = { presetMapper.runtimeToString(it) },
            textToValue = { presetMapper.inputToRuntime(it) },
            showPopup = showPopup,
        )
    }
}

@Composable
fun ListModeButton(
    listMode: ListMode,
    onListModeChanged: (ListMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonSpec =
        when (listMode) {
            ListMode.ALL -> ButtonSpec.AllMoviesFilter
            ListMode.PENDING -> ButtonSpec.PendingFilter
            ListMode.WATCHED -> ButtonSpec.WatchedFilter
        }
    val isSelected = listMode != ListMode.ALL
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }
    Box(
        modifier =
            modifier
                .padding(3.dp)
                .size(width = 120.dp, height = 32.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).clickable {
                    onListModeChanged(listMode.next())
                }.testTag(tag = UiTags.Buttons.LIST_MODE),
    ) {
        Text(
            text = stringResource(id = buttonSpec.labelRes),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            modifier =
                Modifier
                    .padding(8.dp)
                    .size(100.dp)
                    .align(Alignment.CenterStart),
        )
        Icon(
            buttonSpec.icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier =
                Modifier
                    .padding(8.dp)
                    .size(12.dp)
                    .align(Alignment.CenterEnd),
        )
    }
}

@Composable
fun MinMaxButton(
    label: String,
    filterValues: MinMaxFilter,
    range: IntRange,
    onFilterValuesChanged: (MinMaxFilter) -> Unit,
    valueToText: (Int?) -> String,
    textToValue: (String) -> Int?,
    showPopup: (PopupInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = filterValues.isActive
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }

    val minText = filterValues.min?.let { printableRuntimePacked(it) } ?: ""
    val maxText = filterValues.max?.let { printableRuntimePacked(it) } ?: ""
    Box(
        modifier =
            modifier
                .padding(3.dp)
                .size(width = 180.dp, height = 32.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).clickable {
                    if (filterValues.isActive) {
                        onFilterValuesChanged(filterValues.copy(isEnabled = false))
                    } else {
                        showPopup(
                            PopupInfo.NumberChooser(
                                label = label,
                                filterValues = filterValues,
                                range = range,
                                valueToText = valueToText,
                                textToValue = textToValue,
                                onConfirm = onFilterValuesChanged,
                            ),
                        )
                    }
                }.testTag(tag = UiTags.Buttons.RUNTIME_FILTER),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            modifier =
                Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterStart),
        )
        Text(
            text = "$minText - $maxText",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            modifier =
                Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterEnd),
        )
    }
}
