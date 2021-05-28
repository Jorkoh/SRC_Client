package ui.screens.home

import data.SRCRepository
import data.local.*
import data.local.entities.FullGame
import data.local.entities.Run
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
                if (homeUIState.value.game?.gameId != newSelectedGame.id) {
                    refreshGame(newSelectedGame.id)
                }
            }
        }
        scope.launch {
            runsDAO.getSelectedRunId().collect { newSelectedRunId ->
                _homeUIState.value = _homeUIState.value.copy(hasRunSelected = newSelectedRunId != null)
            }
        }
    }

    private val _homeUIState = MutableStateFlow(
        HomeUIState(
            game = null,
            hasRunSelected = false,
            gameSelectorIsOpen = false,
            isRefreshAvailable = false,
            settingsUIState = HomeUIState.SettingsUIState.LoadingSettings,
            runsUIState = HomeUIState.RunsUIState.LoadingRuns
        )
    )
    val homeUIState: StateFlow<HomeUIState> = _homeUIState

    private var fullGameJob: Job? = null
    private var refreshAvailableJob: Job? = null

    private fun refreshGame(newGameId: GameId) {
        fullGameJob?.cancel()

        val gameChanged = _homeUIState.value.game?.gameId != newGameId

        if (gameChanged) {
            _homeUIState.value = _homeUIState.value.copy(
                game = null,
                runsUIState = HomeUIState.RunsUIState.LoadingRuns,
                settingsUIState = HomeUIState.SettingsUIState.LoadingSettings
            )
            settingsDAO.resetGameSpecificSettings()
        } else {
            _homeUIState.value = _homeUIState.value.copy(runsUIState = HomeUIState.RunsUIState.LoadingRuns)
        }

        fullGameJob = scope.launch {
            if (gameChanged) {
                val game = srcRepository.getFullGame(newGameId).first()
                _homeUIState.value = _homeUIState.value.copy(game = game)
            }

            restartRefreshAvailableCooldown()
            srcRepository.cacheRuns(newGameId)
            observeSettings()
        }
    }

    private suspend fun observeSettings() {
        settingsDAO.getSettings().collect { settings ->
            homeUIState.value.game?.let { game ->
                // Could pass the raw runs here from refresh games to derive settings from them
                _homeUIState.value = _homeUIState.value.copy(
                    settingsUIState = HomeUIState.SettingsUIState.LoadedSettings(settings, game)
                )
                filterRuns()
            }
        }
    }

    private suspend fun filterRuns() {
        (homeUIState.value.settingsUIState as? HomeUIState.SettingsUIState.LoadedSettings)?.let { loadedSettings ->
            _homeUIState.value = _homeUIState.value.copy(runsUIState = HomeUIState.RunsUIState.LoadingRuns)
            val runs = srcRepository.getFilteredCachedRuns(loadedSettings.settings)
            _homeUIState.value = _homeUIState.value.copy(
                runsUIState = HomeUIState.RunsUIState.LoadedRuns(runs, loadedSettings.game)
            )
        }
    }

    private fun restartRefreshAvailableCooldown() {
        refreshAvailableJob?.cancel()
        refreshAvailableJob = scope.launch {
            _homeUIState.value = _homeUIState.value.copy(isRefreshAvailable = false)
            delay(REFRESH_AVAILABLE_COOLDOWN)
            _homeUIState.value = _homeUIState.value.copy(isRefreshAvailable = true)
        }
    }

    fun openGameSelector() {
        _homeUIState.value = _homeUIState.value.copy(gameSelectorIsOpen = true)
    }

    fun closeGameSelector() {
        _homeUIState.value = _homeUIState.value.copy(gameSelectorIsOpen = false)
    }

    fun refreshRuns() {
        _homeUIState.value.game?.gameId?.let {
            refreshGame(it)
        }
    }

    fun changeSettings(newSettings: Settings) {
        settingsDAO.setSettings(newSettings)
    }

    fun selectRun(runId: RunId) {
        runsDAO.selectRun(runId)
    }

    companion object {
        const val REFRESH_AVAILABLE_COOLDOWN = 60000L
    }
}

data class HomeUIState(
    val game: FullGame?,
    val hasRunSelected: Boolean,
    val gameSelectorIsOpen: Boolean,
    val isRefreshAvailable: Boolean,
    val settingsUIState: SettingsUIState,
    val runsUIState: RunsUIState,
) {
    sealed class SettingsUIState {
        object LoadingSettings : SettingsUIState()
        class LoadedSettings(val settings: Settings, val game: FullGame) : SettingsUIState()
    }

    sealed class RunsUIState {
        object LoadingRuns : RunsUIState()
        class LoadedRuns(val runs: List<Run> = emptyList(), val game: FullGame) : RunsUIState()
        class FailedToLoadRuns(val message: String) : RunsUIState()
    }
}
