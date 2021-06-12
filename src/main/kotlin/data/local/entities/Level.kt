package data.local.entities

import data.local.LevelId
import ui.screens.home.Displayable

data class Level(
    val levelId: LevelId,
    val name: String,
    val rules: String?,
    val weblink: String
) : Displayable {
    override val uiString: String
        get() = name
}