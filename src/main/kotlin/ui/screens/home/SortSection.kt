package ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.utils.RunSortDirection
import data.local.entities.utils.RunSortDiscriminator
import persistence.database.Settings
import ui.utils.FlowRow

@Composable
fun SortSection(
    uiState: HomeUIState.SettingsUIState,
    onSortChanged: (Settings) -> Unit
) {
    when (uiState) {
        is HomeUIState.SettingsUIState.LoadingSettings -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
            }
        }
        is HomeUIState.SettingsUIState.LoadedSettings -> {
            SortContent(uiState, onSortChanged)
        }
    }
}

@Composable
private fun SortContent(
    uiState: HomeUIState.SettingsUIState.LoadedSettings,
    onSortChanged: (Settings) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        FlowRow(horizontalGap = 24.dp) {
            val runSortDiscriminators = RunSortDiscriminator.values().toList()
            val selectedRunSortDiscriminator = uiState.settings.runSortDiscriminator
            SettingComponent(
                title = "Sort by",
                selectedOption = selectedRunSortDiscriminator,
                options = runSortDiscriminators,
                addAllOption = false,
                onOptionSelected = {
                    onSortChanged(uiState.settings.copy(runSortDiscriminator = it ?: RunSortDiscriminator.Default))
                }
            )

            val runSortDirections = RunSortDirection.values().toList()
            val selectedRunSortDirection = uiState.settings.runSortDirection
            SettingComponent(
                title = "Direction",
                selectedOption = selectedRunSortDirection,
                options = runSortDirections,
                addAllOption = false,
                onOptionSelected = {
                    onSortChanged(uiState.settings.copy(runSortDirection = it ?: RunSortDirection.Default))
                }
            )
        }
    }
}