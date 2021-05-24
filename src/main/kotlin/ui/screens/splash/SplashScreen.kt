package ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen(
    onLoadingFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { SplashViewModel(scope) }

    SplashScreenContent(
        viewModel.splashUIState.collectAsState(),
        onLoadingFinished
    )
}

@Composable
fun SplashScreenContent(
    uiState: State<SplashUIState>,
    onLoadingFinished: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState.value) {
            is SplashUIState.Loading -> Text(
                text = state.message,
                style = MaterialTheme.typography.subtitle1.copy(fontSize = 20.sp)
            )
            is SplashUIState.Done -> onLoadingFinished()
        }
    }
}