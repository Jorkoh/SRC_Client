package data.local.entities

import data.local.*
import ui.screens.home.Displayable
import java.util.*

enum class TimingMethod(val apiString: String, override val uiString: String) : Displayable {
    RealTime("realtime", "RTA"),
    RealTimeNoLoads("realtime_noloads", "RTA-NL"),
    InGame("ingame", "IGT")
}

data class FullGame(
    val gameId: GameId,
    val name: String,
    val abbreviation: String,

    val releaseDate: Date,
    val additionDate: Date?,

    val showMilliseconds: Boolean,
    val requireVerification: Boolean,
    val requireVideo: Boolean,
    val emulatorsAllowed: Boolean,
    val timingMethods: List<TimingMethod>,
    val primaryTimingMethod: TimingMethod,
    val isROMHack: Boolean,

    val gameTypeIds: List<GameTypeId>,
    val platformIds: List<PlatformId>,
    val regionIds: List<RegionId>,
    val genreIds: List<GenreId>,
    val engineIds: List<EngineId>,
    val developerIds: List<DeveloperId>,
    val publisherIds: List<PublisherId>,

    val moderators: List<User>,
    val levels : List<Level>,
    val categories: List<Category>,

    val weblink: String
)