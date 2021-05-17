package ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.CategoryType
import data.local.entities.RunStatus
import data.local.entities.Variable
import data.local.entities.VariableAndValueIds
import persistence.database.Settings
import ui.utils.FlowRow

@Composable
fun FiltersSection(
    uiState: HomeUIState.SettingsUIState,
    onFiltersChanged: (Settings) -> Unit
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
            FiltersContent(uiState, onFiltersChanged)
        }
    }
}

@Composable
private fun FiltersContent(
    uiState: HomeUIState.SettingsUIState.LoadedSettings,
    onFiltersChanged: (Settings) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        val gameVariables = uiState.game.categories.firstOrNull { it.type == CategoryType.PerGame }
            ?.variables?.filter { it.categoryId == null }
        val categories = uiState.game.categories.filter { it.type == CategoryType.PerGame }
        val selectedCategory = categories.firstOrNull { it.categoryId == uiState.settings.categoryId }

        FlowRow(horizontalGap = 24.dp) {
            // Category filter TODO add level support
            SettingComponent(
                title = "Category",
                selectedOption = selectedCategory,
                options = categories,
                onOptionSelected = { newCategory ->
                    onFiltersChanged(uiState.settings.copy(
                        categoryId = newCategory?.categoryId,
                        variablesAndValuesIds = uiState.settings.variablesAndValuesIds.toMutableList()
                            .filter { filterVariable ->
                                // When changing category only keep game variable filters
                                gameVariables?.any { filterVariable.variableId == it.variableId } ?: false
                            }
                    ))
                }
            )
            // Run status filter
            val runStatuses = RunStatus.values().toList()
            val selectedRunStatus = uiState.settings.runStatus
            SettingComponent(
                title = "Status",
                selectedOption = selectedRunStatus,
                options = runStatuses,
                onOptionSelected = { onFiltersChanged(uiState.settings.copy(runStatus = it)) }
            )
        }

        // Custom leaderboard variables filters from game TODO add level support
        if (!gameVariables.isNullOrEmpty()) {
            Column {
                Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
                VariablesFilters(gameVariables, uiState.settings, onFiltersChanged)
            }
        }

        // Custom leaderboard variables filters from selected category
        val categoryVariables = selectedCategory?.variables?.filter { it.categoryId != null }
        AnimatedVisibility(!categoryVariables.isNullOrEmpty()) {
            if (!categoryVariables.isNullOrEmpty()) {
                Column {
                    Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
                    VariablesFilters(categoryVariables, uiState.settings, onFiltersChanged)
                }
            }
        }
    }
}

@Composable
private fun VariablesFilters(
    variables: List<Variable>,
    filters: Settings,
    onFiltersChanged: (Settings) -> Unit
) {
    // Pair the variable with its possible values and the selected one (if exists) on the filter
    val variableValuesAndSelectedList = variables.map { variable ->
        Triple(
            variable,
            variable.values,
            variable.values.firstOrNull { value ->
                value.valueId == filters.variablesAndValuesIds.firstOrNull {
                    variable.variableId == it.variableId
                }?.valueId
            }
        )
    }.sortedByDescending { it.first.isSubCategory }

    FlowRow(
        horizontalGap = 24.dp,
        verticalGap = 8.dp,
    ) {
        for ((variable, values, selectedValue) in variableValuesAndSelectedList) {
            SettingComponent(
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
