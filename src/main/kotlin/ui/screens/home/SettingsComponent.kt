package ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import persistence.database.Game

@Composable
fun SettingsComponent() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { GameSelectorViewModel(scope) }

    SettingsComponentContent(
        uiState = viewModel.gamesSelectorUIState.collectAsState(),
        selectedGame = viewModel.selectedGame.collectAsState(),
        onQueryChanged = viewModel::onQueryChanged,
        onGameSelected = viewModel::onGameSelected,
        onSearchStarted = viewModel::onSearchStarted,
        onSearchStopped = viewModel::onSearchStopped
    )
}

@Composable
fun SettingsComponentContent(
    uiState: State<GamesSelectorUIState>,
    selectedGame: State<Game?>,
    onQueryChanged: (newQuery: String) -> Unit,
    onGameSelected: (newGame: Game) -> Unit,
    onSearchStarted: () -> Unit,
    onSearchStopped: () -> Unit
) {
    val (searchFieldIsFocused, setSearchFieldIsFocused) = remember { mutableStateOf(false) }
    if (searchFieldIsFocused && uiState.value is GamesSelectorUIState.NotSearching) {
        // Removes the focus of the OutlinedTextField once a game is selected from the dropdown
        LocalFocusManager.current.clearFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(20.dp))
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
                .heightIn(0.dp, TextFieldDefaults.MinHeight * 3)
                .border(BorderStroke(2.dp, Color.Black))
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
                        Divider(modifier = Modifier.padding(horizontal = 10.dp))
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
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = game.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}