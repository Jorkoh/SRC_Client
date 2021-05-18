package ui.screens.home

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.Run
import data.local.entities.RunStatus
import data.local.entities.User
import io.kamel.image.KamelImage
import io.kamel.image.lazyImageResource
import ui.screens.home.HomeUIState.RunsUIState.*
import ui.theme.approveGreen
import ui.theme.pendingBlue
import ui.theme.rejectRed
import ui.utils.toSRCString
import java.awt.Desktop
import java.net.URI

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
        Text(text = position.toString(), style = MaterialTheme.typography.h6)
        Text(run.primaryTime.toSRCString())
        RunnerName(run.players)
        RunStatusIndicator(run.runStatus)
    }
}

@Composable
private fun RunnerName(players: List<User>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        players.firstOrNull()?.countryCode?.let {
            KamelImage(
                resource = lazyImageResource(data = "https://www.speedrun.com/images/flags/$it.png"),
                contentDescription = "Country",
                crossfade = true,
                modifier = Modifier.height(12.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        val playerCountIndicator = when (players.size) {
            0 -> "No players"
            1 -> ""
            2 -> " and 1 other"
            else -> " and ${players.size - 1} others"
        }
        Text("${players.firstOrNull()?.name ?: ""}${playerCountIndicator}")
    }
}

@Composable
private fun RunStatusIndicator(status: RunStatus) {
    val statusColor = when (status) {
        RunStatus.Pending -> MaterialTheme.colors.pendingBlue
        RunStatus.Approved -> MaterialTheme.colors.approveGreen
        RunStatus.Rejected -> MaterialTheme.colors.rejectRed
        else -> LocalContentColor.current
    }
    Box(
        modifier = Modifier.background(
            shape = MaterialTheme.shapes.small,
            color = statusColor.copy(alpha = 0.1f)
        ).padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.caption,
            color = statusColor
        )
    }
}