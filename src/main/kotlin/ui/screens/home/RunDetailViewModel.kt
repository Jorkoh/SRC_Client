package ui.screens.home

import data.SRCRepository
import data.local.RunId
import data.local.RunsDAO
import data.local.entities.FullRun
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RunDetailViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val runsDAO by inject<RunsDAO>()
    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            runsDAO.getSelectedRunId().collect { newSelectedRunId ->
                if (runDetailUIState.value.runId != newSelectedRunId) {
                    onSelectedRunIdChanged(newSelectedRunId)
                }
            }
        }
    }

    private val _runDetailUIState = MutableStateFlow<RunDetailUIState>(RunDetailUIState.NoRunSelected)
    val runDetailUIState: StateFlow<RunDetailUIState> = _runDetailUIState

    private var fullRunJob: Job? = null

    private fun onSelectedRunIdChanged(selectedRunId: RunId?) {
        fullRunJob?.cancel()

        if (selectedRunId == null) {
            _runDetailUIState.value = RunDetailUIState.NoRunSelected
        } else {
            _runDetailUIState.value = RunDetailUIState.LoadingRun

            fullRunJob = scope.launch {
                val fullRun = srcRepository.getFullRun(selectedRunId).first()
                _runDetailUIState.value = RunDetailUIState.LoadedRun(run = fullRun)
            }
        }
    }

    fun onRunDeselected() {
        runsDAO.deselectRun()
    }
}

sealed class RunDetailUIState(val runId: RunId?) {
    object NoRunSelected : RunDetailUIState(null)
    object LoadingRun : RunDetailUIState(null)
    class LoadedRun(val run: FullRun) : RunDetailUIState(run.runId)
    class FailedToLoadRun(val message: String) : RunDetailUIState(null)
}