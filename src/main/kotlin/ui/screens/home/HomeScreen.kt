package ui.screens.home

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.res.vectorXmlResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import persistence.database.Settings
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
        onRefreshButtonClicked = viewModel::refreshGame,
        onSettingsChanged = viewModel::changeSettings
    )
}

enum class BackdropSection {
    Filters, Sort
}

@Composable
private fun HomeScreenContent(
    uiState: State<HomeUIState>,
    scope: CoroutineScope,
    onChangeGameButtonClicked: () -> Unit,
    onChangeGameDialogDismissed: () -> Unit,
    onRefreshButtonClicked: () -> Unit,
    onSettingsChanged: (Settings) -> Unit,
) {
    val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
    var backdropSection by remember { mutableStateOf(BackdropSection.Filters) }

    if (uiState.value.gameSelectorIsOpen) {
        GameDialog(onDismiss = onChangeGameDialogDismissed)
    }

    BackdropScaffold(
        scaffoldState = scaffoldState,
        gesturesEnabled = false,
        appBar = {
            val gameName = (uiState.value as? HomeUIState.Ready)?.game?.name ?: "Loading game..."
            val runCount = ((uiState.value as? HomeUIState.Ready)?.runsUIState
                    as? HomeUIState.RunsUIState.LoadedRuns)?.runs?.size
            HomeTopAppBar(
                gameName = gameName,
                runCount = runCount,
                onChangeGameButtonClicked = onChangeGameButtonClicked,
                refreshButtonEnabled = uiState.value is HomeUIState.Ready,
                onRefreshButtonClicked = {
                    onRefreshButtonClicked()
                    scope.launch { scaffoldState.conceal() }
                },
                onFiltersButtonClicked = {
                    scope.launch {
                        with(scaffoldState) {
                            if (backdropSection != BackdropSection.Filters) {
                                conceal()
                                backdropSection = BackdropSection.Filters
                                // TODO this delay lets the backdrop layout so the reveal animation actually works :/
                                delay(40)
                                reveal()
                            } else {
                                if (targetValue == BackdropValue.Concealed) reveal() else conceal()
                            }
                        }
                    }
                },
                onSortButtonClicked = {
                    scope.launch {
                        with(scaffoldState) {
                            if (backdropSection != BackdropSection.Sort) {
                                conceal()
                                backdropSection = BackdropSection.Sort
                                // TODO this delay lets the backdrop layout so the reveal animation actually works :/
                                delay(40)
                                reveal()
                            } else {
                                if (targetValue == BackdropValue.Concealed) reveal() else conceal()
                            }
                        }
                    }
                }
            )
        },
        frontLayerContent = { RunsSection(uiState.value.runsUIState) },
        backLayerContent = {
            when (backdropSection) {
                BackdropSection.Filters -> FiltersSection(
                    uiState = uiState.value.settingsUIState,
                    onFiltersChanged = onSettingsChanged
                )
                BackdropSection.Sort -> SortSection(
                    uiState = uiState.value.settingsUIState,
                    onSortChanged = onSettingsChanged
                )
            }

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
    refreshButtonEnabled: Boolean,
    onRefreshButtonClicked: () -> Unit,
    onFiltersButtonClicked: () -> Unit,
    onSortButtonClicked: () -> Unit
) {
    TopAppBar(
        title = { Text("$gameName ${if (runCount != null && runCount > 0) "($runCount runs)" else ""}") },
        actions = {
            GameDialogButton(onChangeGameButtonClicked)
            RefreshButton(refreshButtonEnabled, onRefreshButtonClicked)
            FiltersButton(onFiltersButtonClicked)
            SortButton(onSortButtonClicked)
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
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh game"
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

@Composable
private fun SortButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        // TODO change this to IconToggleButton?
        Icon(
            imageVector = vectorXmlResource("ic_sort.xml"),
            contentDescription = "Sort"
        )
    }
}