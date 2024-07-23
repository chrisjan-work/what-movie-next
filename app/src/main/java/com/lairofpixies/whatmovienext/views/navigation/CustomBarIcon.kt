package com.lairofpixies.whatmovienext.views.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun CustomBarIcon(
    modifier: Modifier = Modifier,
    specification: ButtonSpec,
) {
    val label = LocalContext.current.getString(specification.labelRes)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = specification.icon,
            contentDescription = label,
            modifier = modifier.padding(start = 16.dp, end = 16.dp),
        )
        Text(
            text = label,
            modifier = modifier,
        )
    }
}
