package ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.local.entities.Run
import java.awt.Desktop
import java.net.URI

@Composable
fun RunsComponent(){
    val scope = rememberCoroutineScope()
    val viewModel = remember { RunsViewModel(scope) }

    RunsComponentContent(
        viewModel.runsUIState.collectAsState()
    )
}

@Composable
private fun RunsComponentContent(uiState: State<RunsUIState>) {
    LazyColumn {
        when (val state = uiState.value) {
            is RunsUIState.FailedToLoad -> item {
                Text(state.message, modifier = Modifier.padding(vertical = 10.dp))
            }
            is RunsUIState.Loading -> item {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
            }
            is RunsUIState.Loaded -> {
                items(state.runs) { run ->
                    RunItem(run)
                }
            }
        }
    }
}

@Composable
private fun RunItem(run: Run) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
//            .clickable { onRunSelected(game) }
            .clickable { Desktop.getDesktop().browse(URI(run.weblink)) }
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = run.runId.value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}