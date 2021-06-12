package data.utils

import ui.screens.home.Displayable

enum class LeaderboardType(override val uiString: String) : Displayable {
    FullGame("FullGame"),
    Level("Level");
}

/**
 * Display one run per player like the leaderboards or unlimited
 */
enum class LeaderboardStyle(override val uiString: String) : Displayable {
    Yes("Yes"),
    No("No");

    companion object {
        val Default = No
    }
}

enum class SearchQueryTarget(override val uiString: String) : Displayable {
    Everywhere("Everywhere"),
    PlayerNames("Player names"),
    CountryCodes("Country codes (ISO 2)"),
    Comment("Comment"),
    RejectionReason("Rejection reason");

    companion object {
        val Default = Everywhere
    }
}