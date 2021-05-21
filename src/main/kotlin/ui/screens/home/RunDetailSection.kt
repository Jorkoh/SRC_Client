package ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun RunDetailSection(hasBackButton: Boolean = false) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { RunDetailViewModel(scope) }

    RunDetailContent(
        uiState = viewModel.runDetailUIState.collectAsState(),
        hasBackButton = hasBackButton,
        onBackPressed = viewModel::onRunDeselected
    )
}

@Composable
private fun RunDetailContent(
    uiState: State<RunDetailViewModel.RunDetailUIState>,
    hasBackButton: Boolean,
    onBackPressed: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasBackButton) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        }
        when (val state = uiState.value) {
            is RunDetailViewModel.RunDetailUIState.FailedToLoadRun -> Text("FAILED TO LOAD RUN")
            is RunDetailViewModel.RunDetailUIState.LoadedRun -> Text("LOADED RUN ${state.run.runId}")
            is RunDetailViewModel.RunDetailUIState.LoadingRun -> Text("LOADING RUN")
            is RunDetailViewModel.RunDetailUIState.NoRunSelected -> Text("NO RUN SELECTED")
        }
    }
}