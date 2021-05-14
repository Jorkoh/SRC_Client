package ui.screens.home

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.res.vectorXmlResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import persistence.database.Filters
import ui.screens.game.GameDialog
import ui.theme.offWhite

@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { HomeViewModel(scope) }

    HomeScreenContent(
        uiState = viewModel.homeUIState.collectAsState(),
        scope = scope,
        onChangeGameButtonClicked = { viewModel.setGameSelectorIsOpen(true) },
        onChangeGameDialogDismissed = { viewModel.setGameSelectorIsOpen(false) },
        onRefreshButtonClicked = viewModel::refreshRuns,
        onFiltersChanged = viewModel::changeFilters
    )
}

@Composable
private fun HomeScreenContent(
    uiState: State<HomeUIState>,
    scope: CoroutineScope,
    onChangeGameButtonClicked: () -> Unit,
    onChangeGameDialogDismissed: () -> Unit,
    onRefreshButtonClicked: () -> Unit,
    onFiltersChanged: (Filters) -> Unit,
) {
    val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)

    if (uiState.value.gameSelectorIsOpen) {
        GameDialog(onDismiss = onChangeGameDialogDismissed)
    }

    BackdropScaffold(
        scaffoldState = scaffoldState,
        appBar = {
            val gameName = (uiState.value as? HomeUIState.Ready)?.game?.name ?: "..."
            val runCount = ((uiState.value as? HomeUIState.Ready)?.runsUIState
                    as? HomeUIState.RunsUIState.LoadedRuns)?.runs?.size
            HomeTopAppBar(
                gameName = gameName,
                runCount = runCount,
                onChangeGameButtonClicked = onChangeGameButtonClicked,
                onRefreshButtonClicked = {
                    onRefreshButtonClicked()
                    scope.launch { scaffoldState.conceal() }
                },
                onFiltersButtonClicked = {
                    scope.launch {
                        with(scaffoldState) { if (isConcealed) reveal() else conceal() }
                    }
                },
            )
        },
        frontLayerContent = { RunsSection(uiState.value.runsUIState) },
        backLayerContent = {
            FiltersSection(
                uiState = uiState.value.filtersUIState,
                onFiltersChanged = onFiltersChanged
            )
        },
        backLayerBackgroundColor = MaterialTheme.colors.offWhite,
        frontLayerShape = MaterialTheme.shapes.large
    )
}

@Composable
private fun HomeTopAppBar(
    gameName: String,
    runCount: Int?,
    onChangeGameButtonClicked: () -> Unit,
    onRefreshButtonClicked: () -> Unit,
    onFiltersButtonClicked: () -> Unit
) {
    TopAppBar(
        title = { Text("$gameName ${if (runCount != null && runCount > 0) "($runCount runs)" else ""}") },
        navigationIcon = { GameDialogButton(onChangeGameButtonClicked) },
        actions = {
            RefreshButton(onRefreshButtonClicked)
            FiltersButton(onFiltersButtonClicked)
        }
    )
}

@Composable
private fun GameDialogButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Change game"
        )
    }
}

@Composable
private fun RefreshButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh"
        )
    }
}

@Composable
private fun FiltersButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        // TODO change this to IconToggleButton?
        Icon(
            imageVector = vectorXmlResource("ic_filter.xml"),
            contentDescription = "Filters"
        )
    }
}