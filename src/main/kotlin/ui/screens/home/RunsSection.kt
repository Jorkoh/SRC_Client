package ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorXmlResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.local.entities.*
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
            is LoadedRuns -> RunList(uiState.runs, uiState.game)
        }
    }
}

@Composable
private fun RunList(runs: List<Run>, game: FullGame) {
    val listState = rememberLazyListState()

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(runs) { index, run ->
                RunItem(index + 1, run, game)
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = listState,
                itemCount = runs.size,
                // TODO update this size
                averageItemSize = 37.dp // Item height plus vertical spacing times two
            )
        )
    }
}

@Composable
private fun RunItem(position: Int, run: Run, game: FullGame) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(text = position.toString(), style = MaterialTheme.typography.h6)
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f)
        ) {
            Row {
                RunTimes(run, game.primaryTimingMethod, game.timingMethods)
                RunnerName(run.players)
            }
            CategoryAndVariables(run, game.categories)
        }
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            SRCButton(run.weblink)
            RunStatusIndicator(run.runStatus)
        }
    }
}

@Composable
private fun RunTimes(
    run: Run,
    primaryTimingMethod: TimingMethod,
    timingMethods: List<TimingMethod>
) {
    timingMethods.sortedByDescending { it == primaryTimingMethod }.forEach { method ->
        // TODO this API mess should have been fixed in the response mapping
        when (method) {
            TimingMethod.RealTime -> run.realTime
            TimingMethod.RealTimeNoLoads -> run.realTimeNoLoads
            TimingMethod.InGame -> run.inGameTime
        }?.let {
            Text("${method.uiString}: ${it.toSRCString()}")
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun CategoryAndVariables(run: Run, categories: List<Category>) {
    val category = categories.first { it.categoryId == run.categoryId }
    val valueLabels = run.variablesAndValuesIds.map { runVariableAndValues ->
        val variable = category.variables.first {
            it.variableId == runVariableAndValues.variableId
        }
        val value = variable.values.first {
            it.valueId == runVariableAndValues.valueId
        }
        Pair(variable, value)
    }.sortedByDescending { it.first.isSubCategory }.map { it.second.label }
    Text(
        text = "${category.name} - ${valueLabels.joinToString()}",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun RunnerName(players: List<User>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        players.firstOrNull()?.countryCode?.let {
            KamelImage(
                resource = lazyImageResource(data = "https://www.speedrun.com/images/flags/$it.png"),
                contentDescription = "Country",
                crossfade = true,
                modifier = Modifier.height(12.dp).border(1.dp, Color.Black)
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
private fun SRCButton(weblink: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { Desktop.getDesktop().browse(URI(weblink)) }.padding(start = 2.dp),
    ) {
        Text(text = "SRC", style = MaterialTheme.typography.button)
        Icon(
            imageVector = vectorXmlResource("ic_open.xml"),
            contentDescription = "Open in speedrun.com"
        )
    }
}

@Composable
private fun RunStatusIndicator(status: RunStatus, modifier: Modifier = Modifier) {
    val statusColor = when (status) {
        RunStatus.Pending -> MaterialTheme.colors.pendingBlue
        RunStatus.Approved -> MaterialTheme.colors.approveGreen
        RunStatus.Rejected -> MaterialTheme.colors.rejectRed
        else -> LocalContentColor.current
    }
    Box(
        modifier = modifier.background(
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