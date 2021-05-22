package ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.local.entities.*
import ui.screens.components.PlayerNames
import ui.screens.components.RunStatusIndicator
import ui.theme.offWhite
import ui.utils.FlowRow
import ui.utils.toSRCString
import java.awt.Desktop
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration

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
    uiState: State<RunDetailUIState>,
    hasBackButton: Boolean,
    onBackPressed: () -> Unit
) {
    Surface(
        color = MaterialTheme.colors.offWhite,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (hasBackButton) {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                when (val state = uiState.value) {
                    is RunDetailUIState.FailedToLoadRun -> Text(state.message)
                    is RunDetailUIState.LoadedRun -> LoadedRun(state.run)
                    is RunDetailUIState.LoadingRun -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
                    }
                    is RunDetailUIState.NoRunSelected -> Text("Select a run to see it here")
                }
            }
        }
    }
}

@Composable
private fun LoadedRun(run: FullRun) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        // TODO times, dates and verifier info
        Section("Metadata") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Times(run.primaryTime, run.realTime, run.realTimeNoLoads, run.inGameTime)
                CategoryAndVariables(run.category, run.variablesAndValuesIds)
                Players(run.players)
                Status(
                    runStatus = run.runStatus,
                    verifierName = run.verifier?.name ?: run.verifierId?.value,
                    verificationDate = run.verificationDate,
                    dateFormat = dateFormat
                )
                Dates(run.runDate, run.submissionDate, dateFormat)
            }
        }
        Section("Videos") {
            VideosText(run.videoLinks, run.videoText)
        }
        run.comment?.let {
            Section("Comment") { Text(it) }
        }
        run.rejectionReason?.let {
            Section("Rejection reason") { Text(it) }
        }
    }
}

@Composable
private fun Times(
    primaryTime: Duration,
    realTime: Duration,
    realTimeNoLoads: Duration,
    inGameTime: Duration
) {
    Row {
        Text("Times:")
        Spacer(Modifier.width(16.dp))
        FlowRow(horizontalGap = 12.dp) {
            Text("Primary ${primaryTime.toSRCString() ?: "No time"}")
            realTime.toSRCString()?.let {
                Text("RTA $it")
            }
            realTimeNoLoads.toSRCString()?.let {
                Text("RTA-NL $it")
            }
            inGameTime.toSRCString()?.let {
                Text("IGT $it")
            }
        }
    }
}

@Composable
private fun CategoryAndVariables(category: Category, variablesAndValuesIds: List<VariableAndValueIds>) {
    val variableAndValuePairings = variablesAndValuesIds.map { runVariableAndValues ->
        val variable = category.variables.first {
            it.variableId == runVariableAndValues.variableId
        }
        val value = variable.values.first {
            it.valueId == runVariableAndValues.valueId
        }
        Pair(variable, value)
    }
    val subCategoryPairings = variableAndValuePairings.filter { it.first.isSubCategory }
    val nonSubCategoryPairings = variableAndValuePairings.filterNot { it.first.isSubCategory }

    Column {
        Row {
            Text("Category:")
            Spacer(Modifier.width(16.dp))
            FlowRow {
                Text("${category.name}${if (subCategoryPairings.isNotEmpty()) " - " else ""}")
                subCategoryPairings.forEachIndexed { index, (_, value) ->
                    Text("${value.label}${if (index != subCategoryPairings.size - 1) ", " else ""}")
                }
            }
        }

        if (nonSubCategoryPairings.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row {
                Text("Variables:")
                Spacer(Modifier.width(16.dp))
                FlowRow {
                    nonSubCategoryPairings.forEachIndexed { index, (variable, value) ->
                        val separator = when (index) {
                            nonSubCategoryPairings.size - 2 -> " and "
                            nonSubCategoryPairings.size - 1 -> ""
                            else -> ", "
                        }
                        Text("${variable.name}: ${value.label}$separator")
                    }
                }
            }
        }
    }
}

@Composable
private fun Players(players: List<User>) {
    Row {
        Text("Players:")
        Spacer(Modifier.width(16.dp))
        PlayerNames(players)
    }
}

@Composable
private fun Status(
    runStatus: RunStatus,
    verifierName: String?,
    verificationDate: Date?,
    dateFormat: SimpleDateFormat
) {
    Row {
        Text("Status:")
        Spacer(Modifier.width(16.dp))
        FlowRow {
            RunStatusIndicator(runStatus)
            verifierName?.let { name ->
                Text(" by $name")
            }
            verificationDate?.let { date ->
                Text(" on ${dateFormat.format(date)}")
            }
        }
    }
}

@Composable
private fun Dates(
    runDate: Date?,
    submissionDate: Date?,
    dateFormat: SimpleDateFormat
) {
    Row {
        Text("Dates:")
        Spacer(Modifier.width(16.dp))
        FlowRow {
            Text("Played on ${runDate?.let { dateFormat.format(it) } ?: "unknown date"}, ")
            Text("submitted on ${submissionDate?.let { dateFormat.format(it) } ?: "unknown date"}")
        }
    }
}

@Composable
private fun VideosText(videoLinks: List<String>, videoText: String?) {
    // Fixed order
    Column {
        (videoLinks.takeLast(1) + videoLinks.dropLast(1)).forEach { videoLink ->
            ClickableText(
                AnnotatedString(
                    text = videoLink,
                    spanStyle = SpanStyle(
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold
                    )
                ),
                onClick = { Desktop.getDesktop().browse(URI(videoLink)) }
            )
        }
        videoText?.let {
            Text(it)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionContainer {
                content()
            }
        }
    }
}