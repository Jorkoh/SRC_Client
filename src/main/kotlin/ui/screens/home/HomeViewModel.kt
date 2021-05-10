package ui.screens.home

import data.SRCRepository
import data.local.FiltersDAO
import data.local.FiltersId
import data.local.GamesDAO
import data.local.entities.FullGame
import data.local.entities.Run
import data.local.entities.RunStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import persistence.database.Filters
import persistence.database.Game

class HomeViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val filters by inject<FiltersDAO>()
    private val games by inject<GamesDAO>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            games.getSelectedGame().collect { newSelectedGame ->
                onSelectedGameChanged(newSelectedGame)
            }
        }
        scope.launch {
            // Filters state isn't isn't tied to db, it's held in FiltersSection
            val filters = filters.getFilters().first()
            setFiltersUIState(HomeUIState.FiltersUIState(filters))
        }
    }

    private val _homeUIState = MutableStateFlow<HomeUIState>(
        HomeUIState.LoadingGame(
            HomeUIState.FiltersUIState(),
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
            filtersUIState = _homeUIState.value.filtersUIState,
            runsUIState = HomeUIState.RunsUIState.LoadedRuns(),
            gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
        )
        fullGameJob = scope.launch {
            fullGame = srcRepository.getFullGame(newSelectedGame.id).first()
            refreshRuns()
            _homeUIState.value = HomeUIState.Ready(
                game = fullGame,
                filtersUIState = _homeUIState.value.filtersUIState,
                runsUIState = HomeUIState.RunsUIState.LoadingRuns,
                gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
            )
        }
    }

    fun refreshRuns() {
        // TODO this needs to apply the filter settings
        runsQueryJob?.cancel()
        setRunsUIState(HomeUIState.RunsUIState.LoadingRuns)
        runsQueryJob = scope.launch {
            srcRepository.getRuns(
                gameId = fullGame.gameId,
                runStatus = homeUIState.value.filtersUIState.value.runStatus
            ).collect { runs ->
                setRunsUIState(HomeUIState.RunsUIState.LoadedRuns(runs))
            }
        }
    }

    fun applyFilters(newFilters: Filters) {
        filters.setFilters(newFilters)
        // Filters state isn't isn't tied to db, it's held in FiltersSection
        setFiltersUIState(HomeUIState.FiltersUIState(newFilters))
        refreshRuns()
    }

    /* UTILS STUFF TO MANAGE STATE CLASS */

    private fun setFiltersUIState(filtersUIState: HomeUIState.FiltersUIState) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(
                filtersUIState = filtersUIState,
                runsUIState = previous.runsUIState,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
            is HomeUIState.Ready -> HomeUIState.Ready(
                game = previous.game,
                filtersUIState = filtersUIState,
                runsUIState = previous.runsUIState,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
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

    fun setGameSelectorIsOpen(isOpen: Boolean) {
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

    data class FiltersUIState(
        val value: Filters = Filters(FiltersId.Default, RunStatus.Default)
    )

    sealed class RunsUIState {
        object LoadingRuns : RunsUIState()
        class LoadedRuns(val runs: List<Run> = emptyList()) : RunsUIState()
        class FailedToLoadRuns(val message: String) : RunsUIState()
    }
}
