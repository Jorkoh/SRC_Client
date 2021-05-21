package data.utils

import ui.screens.home.Displayable

enum class RunSortDiscriminator(val apiString: String, override val uiString: String) : Displayable {
    // TODO this sort won't work when used directly on the API, need to make it type safe
    PrimaryTime("null", "Primary time"),
    RunDate("date", "Date"),
    SubmissionDate("submitted", "Submission date"),
    VerificationDate("verify-data", "Verification date"),
    Status("status", "Status"),
    PlayerCount("null", "Player count"),
    Game("game", "Game"),
    Category("category", "Category"),
    Level("level", "Level"),
    Platform("platform", "Platform"),
    Region("region", "Region"),
    IsEmulated("emulated", "Is emulated");

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
