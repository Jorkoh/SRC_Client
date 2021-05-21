package ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.RunId
import kotlinx.coroutines.CoroutineScope
import persistence.database.Settings
import ui.screens.game.GameDialog

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
        onSettingsChanged = viewModel::changeSettings,
        onRunSelected = viewModel::selectRun
    )
}

@Composable
private fun HomeScreenContent(
    uiState: State<HomeUIState>,
    scope: CoroutineScope,
    onChangeGameButtonClicked: () -> Unit,
    onChangeGameDialogDismissed: () -> Unit,
    onRefreshButtonClicked: () -> Unit,
    onSettingsChanged: (Settings) -> Unit,
    onRunSelected: (RunId) -> Unit
) {
    if (uiState.value.gameSelectorIsOpen) {
        GameDialog(onDismiss = onChangeGameDialogDismissed)
    }

    BoxWithConstraints {
        if (maxWidth.value > 1150) {
            Row(Modifier.fillMaxSize()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.requiredWidthIn(min = 650.dp).fillMaxWidth(0.4f)
                ) {
                    RunListSection(
                        uiState = uiState,
                        scope = scope,
                        onChangeGameButtonClicked = onChangeGameButtonClicked,
                        onRefreshButtonClicked = onRefreshButtonClicked,
                        onSettingsChanged = onSettingsChanged,
                        onRunSelected = onRunSelected
                    )
                }
                RunDetailSection()
            }
        } else {
            if (uiState.value.hasRunSelected) {
                RunDetailSection(hasBackButton = true)
            } else {
                RunListSection(
                    uiState = uiState,
                    scope = scope,
                    onChangeGameButtonClicked = onChangeGameButtonClicked,
                    onRefreshButtonClicked = onRefreshButtonClicked,
                    onSettingsChanged = onSettingsChanged,
                    onRunSelected = onRunSelected
                )
            }
        }
    }
}