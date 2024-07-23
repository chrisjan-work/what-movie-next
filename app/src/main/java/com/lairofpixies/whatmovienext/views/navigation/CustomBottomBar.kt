package com.lairofpixies.whatmovienext.views.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun CustomBottomBar(
    modifier: Modifier = Modifier,
    items: List<CustomBarItem>,
) {
    NavigationBar {
        items.forEach { customBarItem ->
            NavigationBarItem(
                selected = false,
                modifier =
                    customBarItem.buttonSpec.tag?.let { tag ->
                        modifier.testTag(tag)
                    } ?: modifier,
                icon = {
                    CustomBarIcon(
                        modifier,
                        customBarItem.buttonSpec,
                    )
                },
                enabled = customBarItem.enabled,
                onClick = { customBarItem.onClick?.invoke() },
            )
        }
    }
}
