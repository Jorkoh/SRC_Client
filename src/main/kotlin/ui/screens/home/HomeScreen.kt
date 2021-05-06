package ui.screens.home

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.res.vectorXmlResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.theme.offWhite

@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { HomeViewModel(scope) }

    HomeScreenContent(
        uiState = viewModel.homeUIState.collectAsState(),
        scope = scope,
        onChangeGameButtonClicked = viewModel::onChangeGameButtonClicked,
        onChangeGameDialogDismissed = viewModel::onChangeGameDialogDismissed
    )
}

@Composable
private fun HomeScreenContent(
    uiState: State<HomeUIState>,
    scope: CoroutineScope,
    onChangeGameButtonClicked: () -> Unit,
    onChangeGameDialogDismissed: () -> Unit
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
                onFiltersButtonClicked = {
                    scope.launch {
                        with(scaffoldState) { if (isConcealed) reveal() else conceal() }
                    }
                },
                onChangeGameButtonClicked = onChangeGameButtonClicked
            )
        },
        frontLayerContent = { RunsComponent(uiState.value.runsUIState) },
        backLayerContent = { FiltersComponent(uiState.value.filtersUIState) },
        backLayerBackgroundColor = MaterialTheme.colors.offWhite,
        frontLayerShape = MaterialTheme.shapes.large
    )
}

@Composable
private fun HomeTopAppBar(
    gameTitle: String,
    onChangeGameButtonClicked: () -> Unit,
    onFiltersButtonClicked: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "SRC Client - $gameTitle") },
        navigationIcon = { GameDialogButton(onChangeGameButtonClicked) },
        actions = { FiltersButton(onFiltersButtonClicked) }
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