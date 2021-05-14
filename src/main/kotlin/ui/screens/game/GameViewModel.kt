package ui.screens.game

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import data.SRCRepository
import data.local.GamesDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import persistence.database.Game

class GameViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val games by inject<GamesDAO>()
    private val srcRepository by inject<SRCRepository>()

    private val _gamesQueryUIState = MutableStateFlow<GameSelectorUIState>(GameSelectorUIState.NotSearching())
    val gameSelectorUIState: StateFlow<GameSelectorUIState> = _gamesQueryUIState

    var selectedGame: MutableState<Game> = mutableStateOf(games.getSelectedGameBlocking())

    private var queriedGamesJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        queriedGamesJob?.cancel()
        if (newQuery != EMPTY_QUERY) {
            _gamesQueryUIState.value = GameSelectorUIState.LoadingQuery(newQuery)
            queriedGamesJob = scope.launch {
                val games = srcRepository.getGames(newQuery).first()
                _gamesQueryUIState.value = GameSelectorUIState.LoadedQuery(newQuery, games)
            }
        } else {
            _gamesQueryUIState.value = GameSelectorUIState.LoadedQuery(newQuery)
        }
    }

    fun onSearchStarted() {
        _gamesQueryUIState.value = GameSelectorUIState.LoadedQuery(EMPTY_QUERY)
    }

    fun onSearchStopped() {
        queriedGamesJob?.cancel()
        _gamesQueryUIState.value = GameSelectorUIState.NotSearching()
    }

    fun onGameSelected(newGame: Game) {
        selectedGame.value = newGame
        _gamesQueryUIState.value = GameSelectorUIState.NotSearching()
    }

    fun onSave() {
        scope.launch {
            games.setSelectedGameIfChanged(selectedGame.value)
        }
    }

    companion object {
        const val EMPTY_QUERY = ""
    }
}

sealed class GameSelectorUIState(val query: String) {
    class NotSearching : GameSelectorUIState(GameViewModel.EMPTY_QUERY)
    class LoadingQuery(query: String) : GameSelectorUIState(query)
    class LoadedQuery(query: String, val games: List<Game> = emptyList()) : GameSelectorUIState(query)
    class FailedToLoadQuery(query: String, val message: String) : GameSelectorUIState(query)
}