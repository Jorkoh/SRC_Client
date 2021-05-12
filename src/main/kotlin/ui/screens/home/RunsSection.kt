package ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        when (uiState) {
            is FailedToLoadRuns -> item {
                Text(uiState.message, modifier = Modifier.padding(vertical = 10.dp))
            }
            is LoadingRuns -> item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
                }
            }
            is LoadedRuns -> {
                itemsIndexed(uiState.runs) { index, run ->
                    RunItem(index + 1, run)
                }
            }
        }
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