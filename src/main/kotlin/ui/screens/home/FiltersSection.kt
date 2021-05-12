package ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.CategoryType
import data.local.entities.RunStatus
import persistence.database.Filters

@Composable
fun FiltersSection(
    filtersUIState: HomeUIState.FiltersUIState,
    onFiltersChanged: (Filters) -> Unit
) {
    when (filtersUIState) {
        is HomeUIState.FiltersUIState.LoadingFilters -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
            }
        }
        is HomeUIState.FiltersUIState.LoadedFilters -> {
            val selectedRunStatus = filtersUIState.filters.runStatus

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                FilterComponent(
                    title = "Status",
                    selectedOption = selectedRunStatus,
                    options = RunStatus.values().toList(),
                    onOptionSelected = { onFiltersChanged(filtersUIState.filters.copy(runStatus = it)) }
                )
                Spacer(Modifier.width(24.dp))
                FilterComponent(
                    title = "Category",
                    selectedOption = filtersUIState.game.categories.firstOrNull {
                        it.categoryId == filtersUIState.filters.categoryId
                    },
                    options = filtersUIState.game.categories.filter { it.type == CategoryType.PerGame },
                    onOptionSelected = { onFiltersChanged(filtersUIState.filters.copy(categoryId = it?.categoryId)) }
                )
            }
        }
    }
}
