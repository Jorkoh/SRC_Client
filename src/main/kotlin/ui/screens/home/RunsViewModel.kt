package ui.screens.home

import data.SRCRepository
import data.local.SettingsRepository
import data.local.entities.Run
import data.local.entities.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import settings.database.Game

class RunsViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val settings by inject<SettingsRepository>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            settings.getSelectedGame().collect { selectedGame ->
                // TODO move towards setting and observing and entire settings entity
                refreshRuns(selectedGame)
            }
        }
    }

    private var runsQueryJob: Job? = null

    private val _runsUIState = MutableStateFlow<RunsUIState>(RunsUIState.Loaded())
    val runsUIState: StateFlow<RunsUIState> = _runsUIState

    private fun refreshRuns(selectedGame: Game?) {
        runsQueryJob?.cancel()
        if (selectedGame != null) {
            _runsUIState.value = RunsUIState.Loading
            runsQueryJob = scope.launch {
                srcRepository.getRuns(selectedGame.id, Status.Pending).collect { runs ->
                    _runsUIState.value = RunsUIState.Loaded(runs)
                }
            }
        } else {
            _runsUIState.value = RunsUIState.Loaded()
        }
    }
}

sealed class RunsUIState {
    object Loading : RunsUIState()
    class Loaded(val runs: List<Run> = emptyList()) : RunsUIState()
    class FailedToLoad(val message: String) : RunsUIState()
}
