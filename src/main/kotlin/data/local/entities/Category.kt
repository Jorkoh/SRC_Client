package data.local.entities

import data.local.CategoryId
import ui.screens.home.Displayable

enum class PlayerCountType(val apiString: String) {
    Exactly("exactly"),
    UpTo("up-to")
}

enum class CategoryType(val apiString: String) {
    PerGame("per-game"),
    PerLevel("per-level")
}

data class Category(
    val categoryId: CategoryId,
    val name: String,

    val type: CategoryType,
    val rules: String?,
    val playerCountType: PlayerCountType,
    val playerCount: Int,
    val isMiscellaneous: Boolean,
    val variables: List<Variable>,

    val weblink: String
) : Displayable {
    override val uiString: String
        get() = name
}