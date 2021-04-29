package ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorXmlResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.local.entities.Run
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { RunsViewModel(scope) }

    HomeScreenContent(
        uiState = viewModel.runsUIState.collectAsState(),
        scope = scope,
    )
}

@Composable
fun HomeScreenContent(
    uiState: State<RunsUIState>,
    scope: CoroutineScope
) {
    val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)

    BackdropScaffold(
        scaffoldState = scaffoldState,
        appBar = { HomeTopAppBar(scope, scaffoldState) },
        frontLayerContent = { RunsList(uiState) },
        backLayerContent = { SettingsComponent() }
    )
}

@Composable
fun HomeTopAppBar(
    scope: CoroutineScope,
    scaffoldState: BackdropScaffoldState
) {
    TopAppBar(
        title = { Text(text = "SRC Client") },
        actions = {
            if (scaffoldState.isConcealed) {
                IconButton(onClick = { scope.launch { scaffoldState.reveal() } }) {
                    Icon(vectorXmlResource("ic_filter.xml"), contentDescription = "Open filters")
                }
            } else {
                IconButton(onClick = { scope.launch { scaffoldState.conceal() } }) {
                    Icon(vectorXmlResource("ic_filter.xml"), contentDescription = "Close filters")
                }
            }
        }
    )
}

@Composable
fun RunsList(uiState: State<RunsUIState>) {
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
fun RunItem(run: Run) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
//            .clickable { onRunSelected(game) }
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = run.runId.value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}