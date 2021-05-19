package data.utils

import ui.screens.home.Displayable

data class RunSortParameters(
    val discriminator: RunSortDiscriminator,
    val direction: RunSortDirection
)

enum class RunSortDiscriminator(val apiString: String, override val uiString: String) : Displayable {
    Game("game", "Game"),
    Category("category", "Category"),
    Level("level", "Level"),
    Platform("platform", "Platform"),
    Region("region", "Region"),
    IsEmulated("emulated", "Is emulated"),

    // TODO this sort won't work when used directly on the API, need to make it type safe
    PrimaryTime("null", "Primary time"),
    RunDate("date", "Date"),
    SubmissionDate("submitted", "Submission date"),
    VerificationDate("verify-data", "Verification date"),
    Status("status", "Status");

    companion object {
        val Default = SubmissionDate
    }
}

enum class RunSortDirection(val apiString: String, override val uiString: String) : Displayable {
    Ascending("asc", "Ascending"),
    Descending("desc", "Descending");

    companion object {
        val Default = Ascending
    }
}
