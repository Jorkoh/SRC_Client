package ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FiltersComponent(filtersUIState: HomeUIState.FiltersUIState) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text("Hi, I'm a filter")
    }
}
