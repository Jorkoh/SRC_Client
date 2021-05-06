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

    if (uiState.value.gameDialogOpen) {
        GameDialog(onDismiss = onChangeGameDialogDismissed)
    }

    BackdropScaffold(
        scaffoldState = scaffoldState,
        appBar = {
            HomeTopAppBar(
                scope = scope,
                scaffoldState = scaffoldState,
                onChangeGameButtonClicked = onChangeGameButtonClicked
            )
        },
        frontLayerContent = { RunsComponent() },
        backLayerContent = { FiltersComponent() },
        backLayerBackgroundColor = MaterialTheme.colors.offWhite,
        frontLayerShape = MaterialTheme.shapes.large
    )
}

@Composable
private fun HomeTopAppBar(
    scope: CoroutineScope,
    scaffoldState: BackdropScaffoldState,
    onChangeGameButtonClicked: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "SRC Client") },
        navigationIcon = { GameDialogButton(onChangeGameButtonClicked) },
        actions = { FiltersButton(scope, scaffoldState) }
    )
}

@Composable
private fun GameDialogButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        content = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change game"
            )
        })
}

@Composable
private fun FiltersButton(
    scope: CoroutineScope,
    scaffoldState: BackdropScaffoldState
) {
    IconButton(
        onClick = {
            scope.launch {
                if (scaffoldState.isConcealed) scaffoldState.reveal() else scaffoldState.conceal()
            }
        },
        content = {
            // TODO change this to IconToggleButton()
            Icon(
                imageVector = vectorXmlResource("ic_filter.xml"),
                contentDescription = if (scaffoldState.isConcealed) {
                    "Open filters"
                } else {
                    "Close filters"
                }
            )
        })
}