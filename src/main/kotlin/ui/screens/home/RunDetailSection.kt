package ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
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
import data.local.entities.Category
import data.local.entities.FullRun
import data.local.entities.User
import data.local.entities.VariableAndValueIds
import ui.screens.components.PlayerNames
import ui.theme.offWhite
import ui.utils.FlowRow
import java.awt.Desktop
import java.net.URI

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
                    is RunDetailViewModel.RunDetailUIState.FailedToLoadRun -> Text(state.message)
                    is RunDetailViewModel.RunDetailUIState.LoadedRun -> LoadedRun(state.run)
                    is RunDetailViewModel.RunDetailUIState.LoadingRun -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
                    }
                    is RunDetailViewModel.RunDetailUIState.NoRunSelected -> Text("Select a run to see it here")
                }
            }
        }
    }
}

@Composable
private fun LoadedRun(run: FullRun) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TODO times, dates and verifier info
        Section("Metadata") {
            Column {
                CategoryAndVariables(run.category, run.variablesAndValuesIds)
                Spacer(Modifier.height(8.dp))
                Players(run.players)
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
                        val separator = if (index != nonSubCategoryPairings.size - 1) ", " else ""
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
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            SelectionContainer {
                content()
            }
        }
    }
}