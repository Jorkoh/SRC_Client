package ui.screens.home

import data.SRCRepository
import data.local.FiltersDAO
import data.local.entities.FullGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import persistence.database.Game

class HomeViewModel(private val scope: CoroutineScope) : KoinComponent {

    // TODO add view models inside

    private val filters by inject<FiltersDAO>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            filters.getSelectedGame().collect { newSelectedGame ->
                onSelectedGameChanged(newSelectedGame)
            }
        }
    }

    private val _homeUIState = MutableStateFlow(HomeUIState())
    val homeUIState: StateFlow<HomeUIState> = _homeUIState

    // TODO this lateinit is scary tbh
    private lateinit var fullGame: FullGame

    private var fullGameJob: Job? = null

    private fun onSelectedGameChanged(newSelectedGame: Game) {
        fullGameJob?.cancel()
        _homeUIState.value = _homeUIState.value.copy(loadingFilters = true)
        fullGameJob = scope.launch {
            fullGame = srcRepository.getFullGame(newSelectedGame.id).first()
            _homeUIState.value = _homeUIState.value.copy(loadingFilters = false)
        }
    }

    fun onChangeGameButtonClicked() {
        _homeUIState.value = _homeUIState.value.copy(gameDialogOpen = true)
    }

    fun onChangeGameDialogDismissed() {
        _homeUIState.value = _homeUIState.value.copy(gameDialogOpen = false)
    }
}

data class HomeUIState(
    val gameDialogOpen: Boolean = false,
    val loadingFilters: Boolean = false
)