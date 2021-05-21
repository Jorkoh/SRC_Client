package ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.local.entities.RunStatus
import ui.theme.approveGreen
import ui.theme.pendingBlue
import ui.theme.rejectRed

@Composable
fun RunStatusIndicator(status: RunStatus, modifier: Modifier = Modifier) {
    val statusColor = when (status) {
        RunStatus.Pending -> MaterialTheme.colors.pendingBlue
        RunStatus.Approved -> MaterialTheme.colors.approveGreen
        RunStatus.Rejected -> MaterialTheme.colors.rejectRed
        else -> LocalContentColor.current
    }
    Box(
        modifier = modifier.background(
            shape = MaterialTheme.shapes.small,
            color = statusColor.copy(alpha = 0.1f)
        ).padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.caption,
            color = statusColor
        )
    }
}