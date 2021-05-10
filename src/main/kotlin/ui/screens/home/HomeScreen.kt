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
        onApplyFiltersClicked = viewModel::applyFilters
    )
}

@Composable
private fun HomeScreenContent(
    uiState: State<HomeUIState>,
    scope: CoroutineScope,
    onChangeGameButtonClicked: () -> Unit,
    onChangeGameDialogDismissed: () -> Unit,
    onRefreshButtonClicked: () -> Unit,
    onApplyFiltersClicked: (Filters) -> Unit
) {
    val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)

    if (uiState.value.gameSelectorIsOpen) {
        GameDialog(onDismiss = onChangeGameDialogDismissed)
    }

    BackdropScaffold(
        scaffoldState = scaffoldState,
        appBar = {
            HomeTopAppBar(
                gameTitle = (uiState.value as? HomeUIState.Ready)?.game?.name ?: "...",
                onChangeGameButtonClicked = onChangeGameButtonClicked,
                onRefreshButtonClicked = onRefreshButtonClicked,
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
                filtersUIState = uiState.value.filtersUIState,
                onApplyFiltersClicked = {
                    onApplyFiltersClicked(it)
                    scope.launch { scaffoldState.conceal() }
                }
            )
        },
        backLayerBackgroundColor = MaterialTheme.colors.offWhite,
        frontLayerShape = MaterialTheme.shapes.large
    )
}

@Composable
private fun HomeTopAppBar(
    gameTitle: String,
    onChangeGameButtonClicked: () -> Unit,
    onRefreshButtonClicked: () -> Unit,
    onFiltersButtonClicked: () -> Unit
) {
    TopAppBar(
        title = { Text(gameTitle) },
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