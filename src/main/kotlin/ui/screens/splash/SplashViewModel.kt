package ui.screens.splash

import data.SRCRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplashViewModel(private val scope: CoroutineScope) : KoinComponent {

    private val srcRepository by inject<SRCRepository>()

    init {
        scope.launch {
            srcRepository.cacheGamesIfNeeded().collect { message ->
                _splashUIState.value = SplashUIState.Loading(message)
            }
            _splashUIState.value = SplashUIState.Done
        }
    }

    private val _splashUIState = MutableStateFlow<SplashUIState>(SplashUIState.Loading())
    val splashUIState: StateFlow<SplashUIState> = _splashUIState
}

sealed class SplashUIState {
    class Loading(val message: String = "") : SplashUIState()
    object Done : SplashUIState()
}