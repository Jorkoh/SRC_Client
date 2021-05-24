package data.utils

import ui.screens.home.Displayable

enum class RunSortDiscriminator(override val uiString: String) : Displayable {
    PrimaryTime("Primary time"),
    RunDate("Date"),
    SubmissionDate("Submission date"),
    VerificationDate("Verification date"),
    Status("Status"),
    PlayerCount("Player count"),
    Game("Game"),
    Category("Category"),
    Level("Level"),
    Platform("Platform"),
    Region("Region"),
    IsEmulated("Is emulated");

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
