package ui.screens.gameselector

import data.SRCRepository
import data.local.SettingsRepository
import data.local.entities.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.skija.impl.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import settings.database.Game

class GameSelectorViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val settings by inject<SettingsRepository>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            settings.getSelectedGame().collect { newSelectedGame ->
                _selectedGame.value = newSelectedGame
            }
        }
        scope.launch {
            selectedGame.collect { selectedGame ->
                if (selectedGame != null) {
                    srcRepository.getRuns(selectedGame.id, Status.Pending).collect {
                        Log.debug("${it.size}")
                    }
                }
            }
        }
    }

    private var queryJob: Job? = null

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame

    private val _gamesQueryUIState = MutableStateFlow<GamesSelectorUIState>(
        GamesSelectorUIState.NotSearching()
    )
    val gamesSelectorUIState: StateFlow<GamesSelectorUIState> = _gamesQueryUIState

    fun onQueryChanged(newQuery: String) {
        queryJob?.cancel()
        if (newQuery != EMPTY_QUERY) {
            _gamesQueryUIState.value = GamesSelectorUIState.LoadingQuery(newQuery)
            queryJob = scope.launch {
                srcRepository.getGames(newQuery).collect { games ->
                    _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(newQuery, games)
                }
            }
        } else {
            _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(newQuery, emptyList())
        }
    }

    fun onSearchStarted() {
        _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(EMPTY_QUERY, emptyList())
        scope.launch {
            settings.setSelectedGame(null)
        }
    }

    fun onSearchStopped() {
        queryJob?.cancel()
        _gamesQueryUIState.value = GamesSelectorUIState.NotSearching()
    }

    fun onGameSelected(newGame: Game) {
        scope.launch {
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
    class LoadedQuery(query: String, val games: List<Game>) : GamesSelectorUIState(query)
    class FailedToLoadQuery(query: String, val message: String) : GamesSelectorUIState(query)
}