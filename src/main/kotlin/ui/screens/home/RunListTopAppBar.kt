package ui.screens.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.vectorXmlResource

@Composable
fun RunListTopAppBar(
    gameName: String,
    runCount: Int?,
    changeButtonColored: Boolean,
    refreshButtonEnabled: Boolean,
    onRefreshButtonClicked: () -> Unit,
    filterButtonColored: Boolean,
    onFiltersButtonClicked: () -> Unit,
    onChangeGameButtonClicked: () -> Unit
) {
    TopAppBar(
        title = { Text("$gameName ${if (runCount != null && runCount > 0) "($runCount runs)" else ""}") },
        actions = {
            GameDialogButton(changeButtonColored, onChangeGameButtonClicked)
            RefreshButton(refreshButtonEnabled, onRefreshButtonClicked)
            FiltersButton(filterButtonColored, onFiltersButtonClicked)
        }
    )
}

@Composable
private fun GameDialogButton(
    changeButtonColored: Boolean,
    onClick: () -> Unit
) {
    val alpha: Float by animateFloatAsState(
        targetValue = if (changeButtonColored) 1f else 0.65f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Change game",
            tint = LocalContentColor.current.copy(alpha = alpha)
        )
    }
}

@Composable
private fun RefreshButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha: Float by animateFloatAsState(
        targetValue = if(enabled) 0.65f else ContentAlpha.disabled,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    IconButton(
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh game",
            tint = LocalContentColor.current.copy(alpha = alpha)
        )
    }
}

@Composable
private fun FiltersButton(
    filterButtonColored: Boolean,
    onClick: () -> Unit
) {
    val alpha: Float by animateFloatAsState(
        targetValue = if (filterButtonColored) 1f else 0.65f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = vectorXmlResource("ic_filter.xml"),
            contentDescription = "Filters",
            tint = LocalContentColor.current.copy(alpha = alpha)
        )
    }
}