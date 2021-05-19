package ui.screens.home

import data.SRCRepository
import data.local.GameId
import data.local.GamesDAO
import data.local.SettingsDAO
import data.local.SettingsId
import data.local.entities.FullGame
import data.local.entities.Run
import data.utils.LeaderboardStyle
import data.utils.RunSortDirection
import data.utils.RunSortDiscriminator
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

        if (homeUIState.value.settingsUIState is HomeUIState.SettingsUIState.LoadedSettings) {
            /*
             Reset the game filters if they were already loaded, this is iffy but resetting
             after loading new ones and checking for validity wasn't updating the query flow :/
             */
            settingsDAO.resetGameSpecificSettings()
        }

        _homeUIState.value = HomeUIState.LoadingGame(
            gameSelectorIsOpen = _homeUIState.value.gameSelectorIsOpen
        )
        fullGameJob = scope.launch {
            fullGame = srcRepository.getFullGame(newSelectedGameId).first()
            _homeUIState.value = HomeUIState.Ready(
                game = fullGame,
                settingsUIState = _homeUIState.value.settingsUIState,
                runsUIState = _homeUIState.value.runsUIState,
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

/* UTILS STUFF TO MANAGE STATE CLASS */

    private fun setSettingsUIState(settingsUIState: HomeUIState.SettingsUIState) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(previous.gameSelectorIsOpen)
            is HomeUIState.Ready -> HomeUIState.Ready(
                game = previous.game,
                settingsUIState = settingsUIState,
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
                settingsUIState = previous.settingsUIState,
                runsUIState = runsUIState,
                gameSelectorIsOpen = previous.gameSelectorIsOpen
            )
        }
    }

    fun setGameSelectorIsOpen(isOpen: Boolean) {
        _homeUIState.value = when (val previous = _homeUIState.value) {
            is HomeUIState.LoadingGame -> HomeUIState.LoadingGame(isOpen)
            is HomeUIState.Ready -> HomeUIState.Ready(
                settingsUIState = previous.settingsUIState,
                runsUIState = previous.runsUIState,
                game = previous.game,
                gameSelectorIsOpen = isOpen
            )
        }
    }
}

// TODO figure out a proper state nesting solution, right now it's pretty awkward
sealed class HomeUIState(
    val settingsUIState: SettingsUIState,
    val runsUIState: RunsUIState,
    val gameSelectorIsOpen: Boolean,
) {
    class LoadingGame(
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(SettingsUIState.LoadingSettings, RunsUIState.LoadingRuns, gameSelectorIsOpen)

    class Ready(
        val game: FullGame,
        settingsUIState: SettingsUIState,
        runsUIState: RunsUIState,
        gameSelectorIsOpen: Boolean = false
    ) : HomeUIState(settingsUIState, runsUIState, gameSelectorIsOpen)

    sealed class SettingsUIState {
        object LoadingSettings : SettingsUIState()
        class LoadedSettings(
            val settings: Settings = Settings(
                id = SettingsId.Default,
                runStatus = null,
                categoryId = null,
                leaderboardStyle = LeaderboardStyle.Default,
                variablesAndValuesIds = emptyList(),
                runSortDiscriminator = RunSortDiscriminator.Default,
                runSortDirection = RunSortDirection.Default
            ),
            val game: FullGame
        ) : SettingsUIState()
    }

    sealed class RunsUIState {
        object LoadingRuns : RunsUIState()
        class LoadedRuns(val runs: List<Run> = emptyList(), val game: FullGame) : RunsUIState()
        class FailedToLoadRuns(val message: String) : RunsUIState()
    }
}
