package ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import persistence.database.Game
import ui.theme.approveGreen
import ui.theme.rejectRed

@Composable
fun GameDialog(
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { GameViewModel(scope) }

    GameDialogContent(
        uiState = viewModel.gamesSelectorUIState.collectAsState(),
        selectedGame = viewModel.selectedGame,
        onQueryChanged = viewModel::onQueryChanged,
        onGameSelected = viewModel::onGameSelected,
        onSearchStarted = viewModel::onSearchStarted,
        onSearchStopped = viewModel::onSearchStopped,
        onSave = {
            viewModel.onSave()
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Composable
private fun GameDialogContent(
    uiState: State<GamesSelectorUIState>,
    selectedGame: State<Game?>,
    onQueryChanged: (newQuery: String) -> Unit,
    onGameSelected: (newGame: Game) -> Unit,
    onSearchStarted: () -> Unit,
    onSearchStopped: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            title = "SRC Client",
            resizable = false,
            undecorated = true,
            size = IntSize(380, 400)
        )
    ) {
        Surface(
            modifier = Modifier.size(380.dp, 400.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colors.surface,
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.primary)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                GameSelector(
                    uiState = uiState,
                    selectedGame = selectedGame,
                    onQueryChanged = onQueryChanged,
                    onGameSelected = onGameSelected,
                    onSearchStarted = onSearchStarted,
                    onSearchStopped = onSearchStopped,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.rejectRed,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        onClick = onSave,
                        enabled = selectedGame.value != null,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.approveGreen,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun GameSelector(
    uiState: State<GamesSelectorUIState>,
    selectedGame: State<Game?>,
    onQueryChanged: (newQuery: String) -> Unit,
    onGameSelected: (newGame: Game) -> Unit,
    onSearchStarted: () -> Unit,
    onSearchStopped: () -> Unit,
    modifier: Modifier,
) {
    val (searchFieldIsFocused, setSearchFieldIsFocused) = remember { mutableStateOf(false) }
    if (searchFieldIsFocused && uiState.value is GamesSelectorUIState.NotSearching) {
        // Removes the focus of the OutlinedTextField once a game is selected from the dropdown
        LocalFocusManager.current.clearFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
    ) {
        GameSelectorSearchField(
            uiState,
            selectedGame,
            onQueryChanged,
            onSearchStarted,
            onSearchStopped,
            setSearchFieldIsFocused
        )
        GameSelectorDropdown(uiState, onGameSelected)
    }
}

@Composable
fun GameSelectorSearchField(
    uiState: State<GamesSelectorUIState>,
    selectedGame: State<Game?>,
    onQueryChanged: (newQuery: String) -> Unit,
    onSearchStarted: () -> Unit,
    onSearchStopped: () -> Unit,
    setSearchFieldIsFocused: (Boolean) -> Unit
) {
    // TODO make this wider
    OutlinedTextField(
        value = if (uiState.value is GamesSelectorUIState.NotSearching) {
            selectedGame.value?.name ?: ""
        } else {
            uiState.value.query
        },
        onValueChange = onQueryChanged,
        label = {
            Text("Game")
        },
        modifier = Modifier.onFocusChanged { newFocusState ->
            if (newFocusState == FocusState.Active && uiState.value is GamesSelectorUIState.NotSearching) {
                onSearchStarted()
            } else if (newFocusState == FocusState.Inactive && uiState.value !is GamesSelectorUIState.NotSearching) {
                onSearchStopped()
            }
            setSearchFieldIsFocused(newFocusState == FocusState.Active)
        },
        isError = uiState.value is GamesSelectorUIState.NotSearching && selectedGame.value == null,
        trailingIcon = {
            if (uiState.value is GamesSelectorUIState.NotSearching && selectedGame.value == null) {
                Icon(Icons.Default.Warning, null)
            }
        }
    )
}

@Composable
fun GameSelectorDropdown(
    uiState: State<GamesSelectorUIState>,
    onGameSelected: (newGame: Game) -> Unit
) {
    AnimatedVisibility(visible = uiState.value !is GamesSelectorUIState.NotSearching) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .width(TextFieldDefaults.MinWidth)
        ) {
            when (val state = uiState.value) {
                is GamesSelectorUIState.LoadingQuery -> item {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
                }
                is GamesSelectorUIState.FailedToLoadQuery -> item {
                    Text(state.message, modifier = Modifier.padding(vertical = 10.dp))
                }
                is GamesSelectorUIState.LoadedQuery -> {
                    items(state.games) { game ->
                        GameSelectorItem(game, onGameSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun GameSelectorItem(
    game: Game,
    onGameSelected: (newGame: Game) -> Unit
) {
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGameSelected(game) }
            .padding(vertical = 10.dp, horizontal = 5.dp)
    ) {
        Text(
            text = game.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}