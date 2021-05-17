package ui.screens.home

import data.SRCRepository
import data.local.FiltersDAO
import data.local.FiltersId
import data.local.GameId
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

class HomeViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val filtersDAO by inject<FiltersDAO>()
    private val gamesDAO by inject<GamesDAO>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            gamesDAO.getSelectedGame().collect { newSelectedGame ->
                onSelectedGameChanged(newSelectedGame.id)
            }
        }
    }

    private val _homeUIState = MutableStateFlow<HomeUIState>(HomeUIState.LoadingGame())
    val homeUIState: StateFlow<HomeUIState> = _homeUIState

    private lateinit var fullGame: FullGame // lateinit kinda yikes

    private var fullGameJob: Job? = null
    private var runsQueryJob: Job? = null

    private fun onSelectedGameChanged(newSelectedGameId: GameId) {
        fullGameJob?.cancel()
        runsQueryJob?.cancel()

        if (homeUIState.value.filtersUIState is HomeUIState.FiltersUIState.LoadedFilters) {
            /*
             Reset the filters if they were loaded from another game, this is iffy but resetting
             after loading new ones and checking for validity wasn't updating the query flow :/
             */
            filtersDAO.resetFilters()
        }

        _homeUIState.value = HomeUIState.LoadingGame(
            gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
        )
        fullGameJob = scope.launch {
            fullGame = srcRepository.getFullGame(newSelectedGameId).first()
            _homeUIState.value = HomeUIState.Ready(
                game = fullGame,
                filtersUIState = _homeUIState.value.filtersUIState,
                runsUIState = _homeUIState.value.runsUIState,
                gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
            )
            observeFilters()
        }
    }

    private suspend fun observeFilters() {
        filtersDAO.getFilters().collect { filters ->
            (homeUIState.value as? HomeUIState.Ready)?.game?.let { game ->
                setFiltersUIState(HomeUIState.FiltersUIState.LoadedFilters(filters, game))
                refreshRuns()
            }
        }
    }

    private fun refreshRuns() {
        runsQueryJob?.cancel()
        (homeUIState.value.filtersUIState as? HomeUIState.FiltersUIState.LoadedFilters)?.filters?.let { filters ->
            setRunsUIState(HomeUIState.RunsUIState.LoadingRuns)
            runsQueryJob = scope.launch {
                val runs = srcRepository.getCachedRuns(
                    filters = filters
                )
                setRunsUIState(HomeUIState.RunsUIState.LoadedRuns(runs))
            }
        }
    }

    fun refreshGame() {
        (homeUIState.value as? HomeUIState.Ready)?.game?.gameId?.let { onSelectedGameChanged(it) }
    }

    fun changeFilters(newFilters: Filters) {
        filtersDAO.setFilters(newFilters)
    }

/* UTILS STUFF TO MANAGE STATE CLASS */

    private fun setFiltersUIState(filtersUIState: HomeUIState.FiltersUIState) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(previous.gameSelectorIsOpen)
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
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(previous.gameSelectorIsOpen)
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
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(isOpen)
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
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(FiltersUIState.LoadingFilters, RunsUIState.LoadingRuns, gameSelectorIsOpen)

    class Ready(
        val game: FullGame,
        filtersUIState: FiltersUIState,
        runsUIState: RunsUIState,
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(filtersUIState, runsUIState, gameSelectorIsOpen)

    sealed class FiltersUIState {
        object LoadingFilters : FiltersUIState()
        class LoadedFilters(
            val filters: Filters = Filters(
                id = FiltersId.Default,
                runStatus = RunStatus.Default,
                categoryId = null,
                variablesAndValuesIds = emptyList()
            ),
            val game: FullGame
        ) : FiltersUIState()
    }

    sealed class RunsUIState {
        object LoadingRuns : RunsUIState()
        class LoadedRuns(val runs: List<Run> = emptyList()) : RunsUIState()
        class FailedToLoadRuns(val message: String) : RunsUIState()
    }
}
