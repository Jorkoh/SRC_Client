package ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

@Composable
fun FiltersComponent() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { FiltersViewModel(scope) }

    FiltersComponentContent(

    )
}

@Composable
fun FiltersComponentContent(

) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text("Hi, I'm a filter")
    }
}
