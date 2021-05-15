package ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.CategoryType
import data.local.entities.RunStatus
import data.local.entities.Variable
import data.local.entities.VariableAndValueIds
import persistence.database.Filters
import ui.utils.FlowRow

@Composable
fun FiltersSection(
    uiState: HomeUIState.FiltersUIState,
    onFiltersChanged: (Filters) -> Unit
) {
    when (uiState) {
        is HomeUIState.FiltersUIState.LoadingFilters -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp))
            }
        }
        is HomeUIState.FiltersUIState.LoadedFilters -> {
            FiltersRow(uiState, onFiltersChanged)
        }
    }
}

@Composable
private fun FiltersRow(
    uiState: HomeUIState.FiltersUIState.LoadedFilters,
    onFiltersChanged: (Filters) -> Unit
) {
    FlowRow(
        horizontalGap = 24.dp,
        verticalGap = 8.dp,
        modifier = Modifier.padding(16.dp)
    ) {
        val gameVariables = uiState.game.categories.firstOrNull { it.type == CategoryType.PerGame }
            ?.variables?.filter { it.categoryId == null }

        // Category filter TODO add level support
        val categories = uiState.game.categories.filter { it.type == CategoryType.PerGame }
        val selectedCategory = categories.firstOrNull { it.categoryId == uiState.filters.categoryId }
        FilterComponent(
            title = "Category",
            selectedOption = selectedCategory,
            options = categories,
            onOptionSelected = { newCategory ->
                onFiltersChanged(
                    uiState.filters.copy(
                        categoryId = newCategory?.categoryId,
                        variablesAndValuesIds = uiState.filters.variablesAndValuesIds.toMutableList()
                            .filter { filterVariable ->
                                // When changing category only keep game variable filters
                                gameVariables?.any { filterVariable.variableId == it.variableId } ?: false
                            }
                    )
                )
            }
        )

        // Run status filter
        val runStatuses = RunStatus.values().toList()
        val selectedRunStatus = uiState.filters.runStatus
        FilterComponent(
            title = "Status",
            selectedOption = selectedRunStatus,
            options = runStatuses,
            onOptionSelected = { onFiltersChanged(uiState.filters.copy(runStatus = it)) }
        )

        // Custom leaderboard variables filters from selected category
        VariablesFilters(
            selectedCategory?.variables?.filter { it.categoryId != null },
            uiState.filters,
            onFiltersChanged
        )
        // Custom leaderboard variables filters from game TODO add level support
        VariablesFilters(
            gameVariables,
            uiState.filters,
            onFiltersChanged
        )
    }
}

@Composable
private fun VariablesFilters(
    variables: List<Variable>?,
    filters: Filters,
    onFiltersChanged: (Filters) -> Unit
) {
    // Pair the variable with its possible values and the selected one (if exists) on the filter
    val variableValuesAndSelectedList = variables?.map { variable ->
        Triple(
            variable,
            variable.values,
            variable.values.firstOrNull { value ->
                value.valueId == filters.variablesAndValuesIds.firstOrNull {
                    variable.variableId == it.variableId
                }?.valueId
            }
        )
    }?.sortedByDescending { it.first.isSubCategory }

    if (variableValuesAndSelectedList != null) {
        for ((variable, values, selectedValue) in variableValuesAndSelectedList) {
            FilterComponent(
                title = "${variable.name}${if (variable.isSubCategory) "*" else ""}",
                selectedOption = selectedValue,
                options = values,
                onOptionSelected = { newValue ->
                    // Add or remove the variable-value pair from the filter
                    val newFilter = filters.variablesAndValuesIds.toMutableList().apply {
                        removeIf { it.variableId == variable.variableId }
                        newValue?.let { add(VariableAndValueIds(variable.variableId, it.valueId)) }
                    }
                    onFiltersChanged(filters.copy(variablesAndValuesIds = newFilter))
                }
            )
        }
    }
}
