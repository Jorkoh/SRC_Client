package ui.screens.home

import data.SRCRepository
import data.local.*
import data.local.entities.FullGame
import data.local.entities.Run
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import persistence.database.Settings

class HomeViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val settingsDAO by inject<SettingsDAO>()
    private val runsDAO by inject<RunsDAO>()
    private val gamesDAO by inject<GamesDAO>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            gamesDAO.getSelectedGame().collect { newSelectedGame ->
                if (homeUIState.value.gameId != newSelectedGame.id) {
                    onSelectedGameChanged(newSelectedGame.id)
                }
            }
        }
        scope.launch {
            runsDAO.getSelectedRunId().collect { newSelectedRunId ->
                setHasRunSelected(newSelectedRunId != null)
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

        if (homeUIState.value.settingsUIState is HomeUIState.SettingsUIState.LoadedSettings) {
            /*
             Reset the game filters if they were already loaded, this is iffy but resetting
             after loading new ones and checking for validity wasn't updating the query flow :/
             */
            settingsDAO.resetGameSpecificSettings()
        }

        _homeUIState.value = HomeUIState.LoadingGame(
            gameId = newSelectedGameId,
            hasRunSelected = _homeUIState.value.hasRunSelected,
            gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
        )
        fullGameJob = scope.launch {
            fullGame = srcRepository.getFullGame(newSelectedGameId).first()
            _homeUIState.value = HomeUIState.Ready(
                game = fullGame,
                settingsUIState = _homeUIState.value.settingsUIState,
                runsUIState = _homeUIState.value.runsUIState,
                hasRunSelected = _homeUIState.value.hasRunSelected,
                gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
            )
            observeSettings()
        }
    }

    private suspend fun observeSettings() {
        settingsDAO.getSettings().collect { settings ->
            (homeUIState.value as? HomeUIState.Ready)?.game?.let { game ->
                setSettingsUIState(HomeUIState.SettingsUIState.LoadedSettings(settings, game))
                refreshRuns()
            }
        }
    }

    private fun refreshRuns() {
        runsQueryJob?.cancel()
        (homeUIState.value.settingsUIState as? HomeUIState.SettingsUIState.LoadedSettings)?.let { loadedSettings ->
            setRunsUIState(HomeUIState.RunsUIState.LoadingRuns)
            runsQueryJob = scope.launch {
                val runs = srcRepository.getCachedRuns(
                    settings = loadedSettings.settings
                )
                setRunsUIState(HomeUIState.RunsUIState.LoadedRuns(runs, loadedSettings.game))
            }
        }
    }

    fun refreshGame() {
        (homeUIState.value as? HomeUIState.Ready)?.game?.gameId?.let { onSelectedGameChanged(it) }
    }

    fun changeSettings(newSettings: Settings) {
        settingsDAO.setSettings(newSettings)
    }

    fun selectRun(runId: RunId) {
        runsDAO.selectRun(runId)
    }

/* UTILS STUFF TO MANAGE STATE CLASS */

    private fun setSettingsUIState(settingsUIState: HomeUIState.SettingsUIState) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(
                hasRunSelected = previous.hasRunSelected,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
            is HomeUIState.Ready -> HomeUIState.Ready(
                game = previous.game,
                settingsUIState = settingsUIState,
                runsUIState = previous.runsUIState,
                hasRunSelected = previous.hasRunSelected,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
        }
    }

    private fun setRunsUIState(runsUIState: HomeUIState.RunsUIState) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(
                hasRunSelected = previous.hasRunSelected,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
            is HomeUIState.Ready -> HomeUIState.Ready(
                game = previous.game,
                settingsUIState = previous.settingsUIState,
                runsUIState = runsUIState,
                hasRunSelected = previous.hasRunSelected,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
        }
    }

    fun setHasRunSelected(hasRunSelected: Boolean) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(
                hasRunSelected = hasRunSelected,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
            is HomeUIState.Ready -> HomeUIState.Ready(
                settingsUIState = previous.settingsUIState,
                runsUIState = previous.runsUIState,
                game = previous.game,
                hasRunSelected = hasRunSelected,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
        }
    }

    fun setGameSelectorIsOpen(isOpen: Boolean) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(
                hasRunSelected = previous.hasRunSelected,
                gameSelectorIsOpen = isOpen
            )
            is HomeUIState.Ready -> HomeUIState.Ready(
                settingsUIState = previous.settingsUIState,
                runsUIState = previous.runsUIState,
                game = previous.game,
                hasRunSelected = previous.hasRunSelected,
                gameSelectorIsOpen = isOpen
            )
        }
    }
}

// TODO figure out a proper state nesting solution, right now it's pretty awkward
sealed class HomeUIState(
    val gameId: GameId?,
    val settingsUIState: SettingsUIState,
    val runsUIState: RunsUIState,
    val hasRunSelected: Boolean,
    val gameSelectorIsOpen: Boolean,
) {
    class LoadingGame(
        gameId: GameId? = null,
        hasRunSelected: Boolean = false,
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(
        gameId,
        SettingsUIState.LoadingSettings,
        RunsUIState.LoadingRuns,
        hasRunSelected,
        gameSelectorIsOpen
    )

    class Ready(
        val game: FullGame,
        settingsUIState: SettingsUIState,
        runsUIState: RunsUIState,
        hasRunSelected: Boolean,
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(game.gameId, settingsUIState, runsUIState, hasRunSelected, gameSelectorIsOpen)

    sealed class SettingsUIState {
        object LoadingSettings : SettingsUIState()
        class LoadedSettings(
            val settings: Settings,
            val game: FullGame
        ) : SettingsUIState()
    }

    sealed class RunsUIState {
        object LoadingRuns : RunsUIState()
        class LoadedRuns(val runs: List<Run> = emptyList(), val game: FullGame) : RunsUIState()
        class FailedToLoadRuns(val message: String) : RunsUIState()
    }
}
