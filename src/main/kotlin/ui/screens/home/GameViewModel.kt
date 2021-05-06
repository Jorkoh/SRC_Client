package ui.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import data.SRCRepository
import data.local.FiltersDAO
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

    private val filters by inject<FiltersDAO>()
    private val srcRepository by inject<SRCRepository>()

    private val _gamesQueryUIState = MutableStateFlow<GamesSelectorUIState>(GamesSelectorUIState.NotSearching())
    val gamesSelectorUIState: StateFlow<GamesSelectorUIState> = _gamesQueryUIState

    var selectedGame: MutableState<Game?> = mutableStateOf(filters.getSelectedGameBlocking())

    private var queriedGamesJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        queriedGamesJob?.cancel()
        if (newQuery != EMPTY_QUERY) {
            _gamesQueryUIState.value = GamesSelectorUIState.LoadingQuery(newQuery)
            queriedGamesJob = scope.launch {
                val games = srcRepository.getGames(newQuery).first()
                _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(newQuery, games)
            }
        } else {
            _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(newQuery)
        }
    }

    fun onSearchStarted() {
        _gamesQueryUIState.value = GamesSelectorUIState.LoadedQuery(EMPTY_QUERY)
        selectedGame.value = null
    }

    fun onSearchStopped() {
        queriedGamesJob?.cancel()
        _gamesQueryUIState.value = GamesSelectorUIState.NotSearching()
    }

    fun onGameSelected(newGame: Game) {
        selectedGame.value = newGame
        _gamesQueryUIState.value = GamesSelectorUIState.NotSearching()
    }

    fun onSave() {
        scope.launch {
            selectedGame.value?.let { newSelectedGame ->
                filters.setSelectedGameIfChanged(newSelectedGame)
            }
        }
    }

    companion object {
        const val EMPTY_QUERY = ""
    }
}

sealed class GamesSelectorUIState(val query: String) {
    class NotSearching : GamesSelectorUIState(GameViewModel.EMPTY_QUERY)
    class LoadingQuery(query: String) : GamesSelectorUIState(query)
    class LoadedQuery(query: String, val games: List<Game> = emptyList()) : GamesSelectorUIState(query)
    class FailedToLoadQuery(query: String, val message: String) : GamesSelectorUIState(query)
}