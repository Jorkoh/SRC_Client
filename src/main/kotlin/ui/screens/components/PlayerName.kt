package ui.screens.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.local.entities.RegisteredUser
import data.local.entities.User
import io.kamel.image.KamelImage
import io.kamel.image.lazyImageResource
import ui.theme.linkBlue
import ui.utils.FlowRow
import java.awt.Desktop
import java.net.URI

@Composable
fun PlayerNames(
    players: List<User>,
    displayOnlyFirst: Boolean = false,
    clickableNamesWhenPossible: Boolean = false
) {
    when {
        players.isEmpty() -> {
            Text("No players")
        }
        displayOnlyFirst -> {
            val playerCountIndicator = when (players.size) {
                1 -> ""
                2 -> " and 1 other"
                else -> " and ${players.size - 1} others"
            }
            Row {
                PlayerName(players.first(), clickableNamesWhenPossible)
                Text(playerCountIndicator)
            }
        }
        else -> {
            FlowRow {
                players.forEachIndexed { index, player ->
                    val separator = when (index) {
                        players.size - 2 -> " and "
                        players.size - 1 -> ""
                        else -> ", "
                    }
                    PlayerName(player, clickableNamesWhenPossible)
                    Text(separator)
                }
            }
        }
    }
}

@Composable
fun PlayerName(player: User, clickableNameWhenPossible: Boolean) {
    val clickableName = clickableNameWhenPossible && player is RegisteredUser

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (clickableName) Modifier.clickable {
            Desktop.getDesktop().browse(URI((player as RegisteredUser).weblink))
        } else {
            Modifier
        }
    ) {
        player.countryCode?.let {
            KamelImage(
                resource = lazyImageResource(data = "https://www.speedrun.com/images/flags/$it.png"),
                contentDescription = "Country",
                onLoading = { Box(Modifier.size(width = 18.dp, height = 12.dp).border(1.dp, Color.Black)) },
                modifier = Modifier.height(12.dp).border(1.dp, Color.Black)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = player.name,
            color = if (clickableName) MaterialTheme.colors.linkBlue else Color.Unspecified,
            fontWeight = if (clickableName) FontWeight.SemiBold else null
        )
    }
}