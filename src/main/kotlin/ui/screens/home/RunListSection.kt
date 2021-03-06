package ui.screens.home

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorXmlResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.local.RunId
import data.local.entities.Category
import data.local.entities.FullGame
import data.local.entities.Run
import data.local.entities.Variable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import persistence.database.Settings
import ui.screens.components.LoadingIndicator
import ui.screens.components.PlayerNames
import ui.screens.components.RunStatusIndicator
import ui.screens.components.Tooltip
import ui.screens.home.HomeUIState.RunsUIState.*
import ui.theme.offWhite
import ui.utils.toSRCString
import java.awt.Desktop
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RunListSection(
    uiState: State<HomeUIState>,
    scope: CoroutineScope,
    onChangeGameButtonClicked: () -> Unit,
    onRefreshButtonClicked: () -> Unit,
    onSettingsChanged: (Settings) -> Unit,
    onRunSelected: (RunId) -> Unit
) {
    val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)

    BackdropScaffold(
        scaffoldState = scaffoldState,
        gesturesEnabled = true,
        appBar = {
            val gameName = uiState.value.game?.name ?: "Loading game..."
            val runCount = (uiState.value.runsUIState as? LoadedRuns)?.runs?.size ?: -1
            RunListTopAppBar(
                gameName = gameName,
                runCount = runCount,
                changeButtonColored = uiState.value.gameSelectorIsOpen,
                onChangeGameButtonClicked = onChangeGameButtonClicked,
                refreshButtonEnabled = uiState.value.isRefreshAvailable,
                onRefreshButtonClicked = onRefreshButtonClicked,
                filterButtonColored = scaffoldState.targetValue == BackdropValue.Revealed,
                onFiltersButtonClicked = {
                    scope.launch {
                        with(scaffoldState) { if (targetValue == BackdropValue.Concealed) reveal() else conceal() }
                    }
                }
            )
        },
        frontLayerContent = { RunList(uiState.value.runsUIState, onRunSelected) },
        backLayerContent = {
            FiltersSection(
                uiState = uiState.value.settingsUIState,
                onFiltersChanged = onSettingsChanged
            )
        },
        backLayerBackgroundColor = MaterialTheme.colors.offWhite,
        frontLayerShape = MaterialTheme.shapes.large
    )
}


@Composable
private fun RunList(
    uiState: HomeUIState.RunsUIState,
    onRunSelected: (RunId) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is FailedToLoadRuns -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(uiState.message, modifier = Modifier.padding(vertical = 10.dp))
            }
            is LoadingRuns -> LoadingIndicator()
            is LoadedRuns -> LoadedRunList(uiState.runs, uiState.game, onRunSelected)
        }
    }
}

@Composable
private fun LoadedRunList(
    runs: List<Run>,
    game: FullGame,
    onRunSelected: (RunId) -> Unit
) {
    val listState = rememberLazyListState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd") }

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(runs) { index, run ->
                RunItem(
                    position = index + 1,
                    run = run,
                    game = game,
                    dateFormat = dateFormat,
                    onRunClicked = { onRunSelected(run.runId) }
                )
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(listState)
        )
    }
}

@Composable
private fun RunItem(
    position: Int,
    run: Run,
    game: FullGame,
    dateFormat: SimpleDateFormat,
    onRunClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onRunClicked)
            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(text = position.toString(), style = MaterialTheme.typography.h6)
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(run.primaryTime.toSRCString() ?: "No time")
                PlayerNames(players = run.players, displayOnlyFirst = true)
                RunDate(run.runDate, dateFormat)
            }
            Spacer(Modifier.height(2.dp))
            CategoryAndVariables(run, game.categories, game.variables)
            Spacer(Modifier.height(1.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxHeight()
        ) {
            SRCButton(run.weblink)
            RunStatusIndicator(run.runStatus)
        }
    }
}

@Composable
private fun CategoryAndVariables(run: Run, categories: List<Category>, variables: List<Variable>) {
    val category = categories.firstOrNull { it.categoryId == run.categoryId }

    val valueLabels = run.variablesAndValuesIds.map { runVariableAndValues ->
        val variable = variables.firstOrNull {
            it.variableId == runVariableAndValues.variableId
        }
        val value = variable?.values?.firstOrNull {
            it.valueId == runVariableAndValues.valueId
        }
        Pair(variable, value)
    }.sortedByDescending { it.first?.isSubCategory ?: false }.map { it.second?.label ?: "Unknown variable" }

    val variablesString = "${if (valueLabels.isNotEmpty()) " - " else ""}${valueLabels.joinToString()}"
    val categoryAndVariablesString = "${category?.name ?: "Unknown category"}$variablesString"

    Text(
        text = categoryAndVariablesString,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun RunDate(
    runDate: Date?,
    dateFormat: SimpleDateFormat
) {
    Text(if (runDate != null) dateFormat.format(runDate) else "Unknown date")
}

@Composable
private fun SRCButton(weblink: String) {
    Tooltip("Open in speedrun.com") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { Desktop.getDesktop().browse(URI(weblink)) }
                .padding(start = 4.dp, end = 2.dp, bottom = 2.dp, top = 0.dp),
        ) {
            Text(text = "SRC", style = MaterialTheme.typography.button)
            Icon(
                imageVector = vectorXmlResource("ic_open.xml"),
                contentDescription = "Open in speedrun.com"
            )
        }
    }
}