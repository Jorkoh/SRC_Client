package ui.screens.home

import data.SRCRepository
import data.local.FiltersDAO
import data.local.entities.FullGame
import data.local.entities.Run
import data.local.entities.Status
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

    private val filters by inject<FiltersDAO>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            filters.getSelectedGame().collect { newSelectedGame ->
                onSelectedGameChanged(newSelectedGame)
            }
        }
    }

    private val _homeUIState = MutableStateFlow<HomeUIState>(
        HomeUIState.LoadingGame(
            HomeUIState.FiltersUIState.PlaceHolder,
            HomeUIState.RunsUIState.LoadedRuns()
        )
    )
    val homeUIState: StateFlow<HomeUIState> = _homeUIState

    private lateinit var fullGame: FullGame // lateinit kinda yikes

    private var fullGameJob: Job? = null
    private var runsQueryJob: Job? = null

    private fun onSelectedGameChanged(newSelectedGame: Game) {
        fullGameJob?.cancel()
        _homeUIState.value = HomeUIState.LoadingGame(
            filtersUIState = HomeUIState.FiltersUIState.PlaceHolder,
            runsUIState = HomeUIState.RunsUIState.LoadedRuns(),
            gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
        )
        fullGameJob = scope.launch {
            fullGame = srcRepository.getFullGame(newSelectedGame.id).first()
            refreshRuns()
            _homeUIState.value = HomeUIState.Ready(
                game = fullGame,
                filtersUIState = HomeUIState.FiltersUIState.PlaceHolder,
                runsUIState = HomeUIState.RunsUIState.LoadingRuns,
                gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
            )
        }
    }

    fun onChangeGameButtonClicked() {
        setGameSelectorIsOpen(true)
    }

    fun onChangeGameDialogDismissed() {
        setGameSelectorIsOpen(false)
    }

    fun refreshRuns() {
        // TODO this needs to apply the filter settings
        runsQueryJob?.cancel()
        setRunsUIState(HomeUIState.RunsUIState.LoadingRuns)
        runsQueryJob = scope.launch {
            srcRepository.getRuns(fullGame.gameId, Status.Pending).collect { runs ->
                setRunsUIState(HomeUIState.RunsUIState.LoadedRuns(runs))
            }
        }
    }

    private fun setGameSelectorIsOpen(isOpen: Boolean) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(
                filtersUIState = previous.filtersUIState,
                runsUIState = previous.runsUIState,
                gameSelectorIsOpen = isOpen
            )
            is HomeUIState.Ready -> HomeUIState.Ready(
                filtersUIState = previous.filtersUIState,
                runsUIState = previous.runsUIState,
                game = previous.game,
                gameSelectorIsOpen = isOpen
            )
        }
    }

    private fun setRunsUIState(runsUIState: HomeUIState.RunsUIState) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(
                filtersUIState = previous.filtersUIState,
                runsUIState = runsUIState,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
            is HomeUIState.Ready -> HomeUIState.Ready(
                game = previous.game,
                filtersUIState = previous.filtersUIState,
                runsUIState = runsUIState,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
        }
    }
}

// TODO figure out a proper state nesting solution, right now it's pretty awkward
sealed class HomeUIState(
    val filtersUIState: FiltersUIState,
    val runsUIState: RunsUIState,
    val gameSelectorIsOpen: Boolean,
) {
    class LoadingGame(
        filtersUIState: FiltersUIState,
        runsUIState: RunsUIState,
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(filtersUIState, runsUIState, gameSelectorIsOpen)

    class Ready(
        val game: FullGame,
        filtersUIState: FiltersUIState,
        runsUIState: RunsUIState,
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(filtersUIState, runsUIState, gameSelectorIsOpen)

    sealed class FiltersUIState {
        object PlaceHolder : FiltersUIState()
    }

    sealed class RunsUIState {
        object LoadingRuns : RunsUIState()
        class LoadedRuns(val runs: List<Run> = emptyList()) : RunsUIState()
        class FailedToLoadRuns(val message: String) : RunsUIState()
    }
}
