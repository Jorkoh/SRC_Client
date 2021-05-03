package ui.screens.home

import data.SRCRepository
import data.local.SettingsDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import persistence.database.Game

class GameSelectorViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val settings by inject<SettingsDAO>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            settings.getSelectedGame().collect { newSelectedGame ->
                _selectedGame.value = newSelectedGame
            }
        }
    }

    private var gamesQueryJob: Job? = null

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame

    private val _gamesQueryUIState = MutableStateFlow<GamesSelectorUIState>(
        GamesSelectorUIState.NotSearching()
    )
    val gamesSelectorUIState: StateFlow<GamesSelectorUIState> = _gamesQueryUIState

    fun onQueryChanged(newQuery: String) {
        gamesQueryJob?.cancel()
        if (newQuery != EMPTY_QUERY) {
            _gamesQueryUIState.value = GamesSelectorUIState.LoadingQuery(newQuery)
            gamesQueryJob = scope.launch {
                srcRepository.getGames(newQuery).collect { games ->
                    _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(newQuery, games)
                }
            }
        } else {
            _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(newQuery)
        }
    }

    fun onSearchStarted() {
        _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(EMPTY_QUERY)
        scope.launch {
            settings.setSelectedGame(null)
        }
    }

    fun onSearchStopped() {
        gamesQueryJob?.cancel()
        _gamesQueryUIState.value = GamesSelectorUIState.NotSearching()
    }

    fun onGameSelected(newGame: Game) {
        scope.launch {
            // TODO move towards setting and observing and entire settings entity
            // TODO keep things on memory until an apply button is clicked and then persist it
            settings.setSelectedGame(newGame)
        }
        _gamesQueryUIState.value = GamesSelectorUIState.NotSearching()
    }

    companion object {
        const val EMPTY_QUERY = ""
    }
}

sealed class GamesSelectorUIState(val query: String) {
    class NotSearching : GamesSelectorUIState(GameSelectorViewModel.EMPTY_QUERY)
    class LoadingQuery(query: String) : GamesSelectorUIState(query)
    class LoadedQuery(query: String, val games: List<Game> = emptyList()) : GamesSelectorUIState(query)
    class FailedToLoadQuery(query: String, val message: String) : GamesSelectorUIState(query)
}