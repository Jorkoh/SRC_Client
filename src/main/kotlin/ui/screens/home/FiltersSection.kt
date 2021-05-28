package ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import data.local.CategoryId
import data.local.LevelId
import data.local.entities.*
import data.utils.LeaderboardStyle
import data.utils.RunSortDirection
import data.utils.RunSortDiscriminator
import data.utils.SearchQueryTarget
import persistence.database.Settings
import ui.screens.components.LoadingIndicator
import ui.utils.FlowRow

@Composable
fun FiltersSection(
    uiState: HomeUIState.SettingsUIState,
    onFiltersChanged: (Settings) -> Unit
) {
    when (uiState) {
        is HomeUIState.SettingsUIState.LoadingSettings -> {
            LoadingIndicator()
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
        // The available categories depend on the selected level (or lack of it)
        val availableCategories = getAvailableCategories(
            selectedLevelId = if (uiState.game.levels.isEmpty()) LevelId.FullGame else uiState.settings.levelId,
            categories = uiState.game.categories
        )
        val selectedCategory = availableCategories.firstOrNull { it.categoryId == uiState.settings.categoryId }

        // The available variables depend on the selected level (or lack of it) and the selected category
        val availableVariables = getAvailableVariables(
            selectedLevelId = if (uiState.game.levels.isEmpty()) LevelId.FullGame else uiState.settings.levelId,
            selectedCategoryId = selectedCategory?.categoryId,
            variables = uiState.game.variables
        )

        // Filter by text
        QueryFilter(uiState.settings, onFiltersChanged)

        // Level, category, run status, verifier and "leaderboard" view
        Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
        GlobalFilters(
            categories = availableCategories,
            selectedCategory = selectedCategory,
            levels = uiState.game.levels,
            variables = uiState.game.variables,
            verifiers = uiState.game.moderators,
            settings = uiState.settings,
            onFiltersChanged = onFiltersChanged
        )

        // Custom leaderboard variables filters
        AnimatedVisibility(availableVariables.isNotEmpty()) {
            Column {
                Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
                VariablesFilters(availableVariables, uiState.settings, onFiltersChanged)
            }
        }

        // Sorting
        Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp))
        SortingComponent(uiState.settings, onFiltersChanged)
    }
}

private fun getAvailableCategories(
    selectedLevelId: LevelId?,
    categories: List<Category>
) = when (selectedLevelId) {
    null -> categories
    LevelId.FullGame -> categories.filter { it.type == CategoryType.PerGame }
    // seems like per-level categories aren't tied to a specific level
    else -> categories.filter { it.type == CategoryType.PerLevel }
}

private fun getAvailableVariables(
    selectedLevelId: LevelId?,
    selectedCategoryId: CategoryId?,
    variables: List<Variable>
) = when (selectedLevelId) {
    null -> variables.filter { selectedCategoryId == null || it.categoryId == selectedCategoryId }
    LevelId.FullGame -> variables.filter {
        (it.scope == VariableScope.Global || it.scope == VariableScope.FullGame)
                && (selectedCategoryId == null || it.categoryId == null || it.categoryId == selectedCategoryId)
    }
    else -> variables.filter {
        (it.scope == VariableScope.Global || it.scope == VariableScope.AllLevels
                || (it.scope == VariableScope.SingleLevel && it.levelId == selectedLevelId))
                && (selectedCategoryId == null || it.categoryId == null || it.categoryId == selectedCategoryId)
    }
}

@Composable
private fun QueryFilter(
    settings: Settings,
    onFiltersChanged: (Settings) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = settings.searchQuery,
            onValueChange = { onFiltersChanged(settings.copy(searchQuery = it)) },
            label = { Text("Search") },
            trailingIcon = {
                AnimatedVisibility(
                    visible = settings.searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Delete query",
                        modifier = Modifier.clickable { onFiltersChanged(settings.copy(searchQuery = "")) }
                    )
                }
            }
        )

        val searchQueryTargets = SearchQueryTarget.values().toList()
        val searchQueryTarget = settings.searchQueryTarget
        SettingComponent(
            title = "Where",
            selectedOption = searchQueryTarget,
            options = searchQueryTargets,
            addAllOption = false,
            onOptionSelected = { onFiltersChanged(settings.copy(searchQueryTarget = it ?: SearchQueryTarget.Default)) },
            modifier = Modifier.alpha(if (settings.searchQuery.isNotEmpty()) 1f else 0.4f)
        )
    }
}

@Composable
private fun GlobalFilters(
    categories: List<Category>,
    selectedCategory: Category?,
    levels: List<Level>,
    variables: List<Variable>,
    settings: Settings,
    onFiltersChanged: (Settings) -> Unit,
    verifiers: List<RegisteredUser>
) {
    FlowRow(
        horizontalGap = 24.dp,
        verticalGap = 8.dp,
    ) {
        // Level filter
        if (levels.isNotEmpty()) {
            val levelsWithFull = listOf(Level.FullGame).plus(levels)
            val selectedLevel = levelsWithFull.firstOrNull { it.levelId == settings.levelId }
            SettingComponent(
                title = "Level",
                selectedOption = selectedLevel,
                options = levelsWithFull,
                onOptionSelected = { newLevel ->
                    onFiltersChanged(settings.copy(
                        levelId = newLevel?.levelId,
                        // Remove category filter if no longer available
                        categoryId = getAvailableCategories(
                            selectedLevelId = newLevel?.levelId,
                            categories = categories
                        ).firstOrNull { it.categoryId == selectedCategory?.categoryId }?.categoryId,
                        // Remove all variable filters no longer available
                        variablesAndValuesIds = settings.variablesAndValuesIds.toMutableList()
                            .filter { filterVariable ->
                                getAvailableVariables(
                                    selectedLevelId = newLevel?.levelId,
                                    selectedCategoryId = selectedCategory?.categoryId,
                                    variables = variables
                                ).any { filterVariable.variableId == it.variableId }
                            }
                    ))
                }
            )
        }

        // Category filter
        SettingComponent(
            title = "Category",
            selectedOption = selectedCategory,
            options = categories,
            onOptionSelected = { newCategory ->
                onFiltersChanged(settings.copy(
                    categoryId = newCategory?.categoryId,
                    // Remove all variable filters no longer available
                    variablesAndValuesIds = settings.variablesAndValuesIds.toMutableList()
                        .filter { filterVariable ->
                            getAvailableVariables(
                                selectedLevelId = settings.levelId,
                                selectedCategoryId = newCategory?.categoryId,
                                variables = variables
                            ).any { filterVariable.variableId == it.variableId }
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

        // Verifier filter
        val selectedVerifier = verifiers.firstOrNull { it.userId == settings.verifierId }
        SettingComponent(
            title = "Verifier",
            selectedOption = selectedVerifier,
            options = verifiers,
            onOptionSelected = { onFiltersChanged(settings.copy(verifierId = it?.userId)) }
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
    FlowRow(
        horizontalGap = 24.dp,
        verticalGap = 8.dp,
    ) {
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