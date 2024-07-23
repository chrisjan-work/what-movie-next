package com.lairofpixies.whatmovienext.views.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun CustomNavigationBar(
    items: List<CustomBarItem>,
    navController: NavController,
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { customBarItem ->
            NavigationBarItem(
                selected = currentRoute == customBarItem.navigationItem.route,
                modifier =
                    customBarItem.navigationItem.tag?.let { tag ->
                        Modifier.testTag(tag)
                    } ?: Modifier,
                icon = { CustomBarIcon(customBarItem.navigationItem) },
                enabled = customBarItem.enabled,
                onClick = {
                    customBarItem.onClick?.invoke()
                        ?: customBarItem.navigationItem.route?.let {
                            navController.navigate(it)
                        }
                        ?: throw IllegalArgumentException("No route for navigation item ${customBarItem.navigationItem.label}")
                },
            )
        }
    }
}

@Composable
fun CustomBarIcon(
    navigationItem: NavigationItem,
    modifier: Modifier = Modifier,
) {
    val label = LocalContext.current.getString(navigationItem.label)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = navigationItem.icon,
            contentDescription = label,
            modifier = modifier.padding(start = 16.dp, end = 16.dp),
        )
        Text(
            text = label,
            modifier = modifier,
        )
    }
}
