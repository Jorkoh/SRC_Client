package ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.*
import data.utils.LeaderboardStyle
import data.utils.RunSortDirection
import data.utils.RunSortDiscriminator
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

        // Filter by text
        QueryFilter(uiState.settings, onFiltersChanged)

        // Category, run status, "leaderboard" view
        Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
        GlobalFilters(
            categories = categories,
            selectedCategory = selectedCategory,
            gameVariables = gameVariables,
            settings = uiState.settings,
            onFiltersChanged = onFiltersChanged
        )

        // Custom leaderboard variables filters from game TODO add level support
        if (!gameVariables.isNullOrEmpty()) {
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
            VariablesFilters(gameVariables, uiState.settings, onFiltersChanged)
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

        // Sorting
        Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
        SortingComponent(uiState.settings, onFiltersChanged)
    }
}

@Composable
private fun QueryFilter(
    settings: Settings,
    onFiltersChanged: (Settings) -> Unit
) {
    OutlinedTextField(
        value = settings.filterQuery,
        onValueChange = { onFiltersChanged(settings.copy(filterQuery = it)) },
        label = { Text("Search") },
        trailingIcon = {
            AnimatedVisibility(
                visible = settings.filterQuery.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Delete query",
                    modifier = Modifier.clickable { onFiltersChanged(settings.copy(filterQuery = "")) }
                )
            }
        }
    )
}

@Composable
private fun GlobalFilters(
    categories: List<Category>,
    selectedCategory: Category?,
    gameVariables: List<Variable>?,
    settings: Settings,
    onFiltersChanged: (Settings) -> Unit
) {
    FlowRow(horizontalGap = 24.dp) {
        // Category filter TODO add level support
        SettingComponent(
            title = "Category",
            selectedOption = selectedCategory,
            options = categories,
            onOptionSelected = { newCategory ->
                onFiltersChanged(settings.copy(
                    categoryId = newCategory?.categoryId,
                    variablesAndValuesIds = settings.variablesAndValuesIds.toMutableList()
                        .filter { filterVariable ->
                            // When changing category only keep game variable filters TODO is this its best place?
                            gameVariables?.any { filterVariable.variableId == it.variableId } ?: false
                        }
                ))
            }
        )

        // Run status filter
        val runStatuses = RunStatus.values().toList()
        val selectedRunStatus = settings.runStatus
        SettingComponent(
            title = "Status",
            selectedOption = selectedRunStatus,
            options = runStatuses,
            onOptionSelected = { onFiltersChanged(settings.copy(runStatus = it)) }
        )

        // Display one run per player thingy
        val leaderboardStyles = LeaderboardStyle.values().toList()
        val selectedLeaderboardStyle = settings.leaderboardStyle
        SettingComponent(
            title = "One run per player",
            selectedOption = selectedLeaderboardStyle,
            options = leaderboardStyles,
            addAllOption = false,
            onOptionSelected = { onFiltersChanged(settings.copy(leaderboardStyle = it ?: LeaderboardStyle.Default)) }
        )
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

@Composable
private fun SortingComponent(
    settings: Settings,
    onSortingChanged: (Settings) -> Unit
) {
    FlowRow(horizontalGap = 24.dp) {
        val runSortDiscriminators = RunSortDiscriminator.values().toList()
        val selectedRunSortDiscriminator = settings.runSortDiscriminator
        SettingComponent(
            title = "Sort by",
            selectedOption = selectedRunSortDiscriminator,
            options = runSortDiscriminators,
            addAllOption = false,
            onOptionSelected = {
                onSortingChanged(settings.copy(runSortDiscriminator = it ?: RunSortDiscriminator.Default))
            }
        )

        val runSortDirections = RunSortDirection.values().toList()
        val selectedRunSortDirection = settings.runSortDirection
        SettingComponent(
            title = "Direction",
            selectedOption = selectedRunSortDirection,
            options = runSortDirections,
            addAllOption = false,
            onOptionSelected = {
                onSortingChanged(settings.copy(runSortDirection = it ?: RunSortDirection.Default))
            }
        )
    }
}