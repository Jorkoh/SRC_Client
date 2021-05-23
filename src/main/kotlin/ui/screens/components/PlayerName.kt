package ui.screens.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.local.entities.User
import io.kamel.image.KamelImage
import io.kamel.image.lazyImageResource
import ui.utils.FlowRow

@Composable
fun PlayerNames(
    players: List<User>,
    displayOnlyFirst: Boolean = false
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
            PlayerName(players.first(), playerCountIndicator)
        }
        else -> {
            FlowRow {
                players.forEachIndexed { index, player ->
                    val separator = when (index) {
                        players.size - 2 -> " and "
                        players.size - 1 -> ""
                        else -> ", "
                    }
                    PlayerName(player, separator)
                }
            }
        }
    }
}

@Composable
private fun PlayerName(player: User, postText: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        player.countryCode?.let {
            KamelImage(
                resource = lazyImageResource(data = "https://www.speedrun.com/images/flags/$it.png"),
                contentDescription = "Country",
                modifier = Modifier.height(12.dp).border(1.dp, Color.Black)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text("${player.name}$postText")
    }
}