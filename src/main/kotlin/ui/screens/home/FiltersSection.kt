package ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import data.local.FiltersId
import data.local.entities.RunStatus
import persistence.database.Filters
import ui.theme.approveGreen

@Composable
fun FiltersSection(
    filtersUIState: HomeUIState.FiltersUIState,
    onApplyFiltersClicked: (Filters) -> Unit
) {
    var selectedRunStatus by remember(filtersUIState.value.runStatus) { mutableStateOf(filtersUIState.value.runStatus) }

    Row(modifier = Modifier.fillMaxWidth()) {
        FilterComponent(
            title = "Status",
            selectedOption = selectedRunStatus,
            options = RunStatus.values().toList(),
            onOptionSelected = { selectedRunStatus = it }
        )

        TextButton(
            onClick = {
                onApplyFiltersClicked(
                    Filters(
                        id = FiltersId.Default,
                        runStatus = selectedRunStatus
                    )
                )
            },
//            enabled = ?,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.approveGreen,
                contentColor = MaterialTheme.colors.onPrimary
            )
        ) {
            Text("Save filters")
        }
    }
}
