package ui.screens.home

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.Run
import ui.screens.home.HomeUIState.RunsUIState.*
import java.awt.Desktop
import java.net.URI
import kotlin.math.roundToInt
import kotlin.time.Duration

@Composable
fun RunsSection(uiState: HomeUIState.RunsUIState) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is FailedToLoadRuns -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(uiState.message, modifier = Modifier.padding(vertical = 10.dp))
            }
            is LoadingRuns -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
            }
            is LoadedRuns -> RunList(uiState.runs)
        }
    }
}

@Composable
private fun RunList(runs: List<Run>) {
    val listState = rememberLazyListState()

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(runs) { index, run ->
                RunItem(index + 1, run)
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = listState,
                itemCount = runs.size,
                averageItemSize = 37.dp // Item height plus vertical spacing times two
            )
        )
    }
}

@Composable
private fun RunItem(position: Int, run: Run) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Desktop.getDesktop().browse(URI(run.weblink)) }
            .padding(vertical = 8.dp, horizontal = 24.dp)
    ) {
        Text(position.toString())
        Spacer(Modifier.width(20.dp))
        Text(run.primaryTime.toSRCString())
        Spacer(Modifier.width(20.dp))
        Text(run.players.first().name)
    }
}

private fun Duration.toSRCString() = toComponents { hours, minutes, seconds, nanoseconds ->
    // TODO added rounding because of some floating point weirdness with Duration, should be fixed properly
    val milliseconds = (nanoseconds / 1000000.0).roundToInt()

    StringBuilder()
        .append(if (hours != 0) "${hours}h " else "")
        .append(if (minutes != 0) "${minutes}m " else "")
        .append(if (seconds != 0) "${seconds}s " else "")
        .append(if (milliseconds != 0) "${milliseconds}ms" else "")
        .toString()
}