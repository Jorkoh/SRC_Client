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
import ui.screens.home.HomeUIState.RunsUIState.*
import java.awt.Desktop
import java.net.URI

@Composable
fun RunsComponent(uiState: HomeUIState.RunsUIState) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        when (uiState) {
            is FailedToLoadRuns -> item {
                Text(uiState.message, modifier = Modifier.padding(vertical = 10.dp))
            }
            is LoadingRuns -> item {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
            }
            is LoadedRuns -> {
                items(uiState.runs) { run ->
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