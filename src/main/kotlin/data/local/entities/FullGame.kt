package data.local.entities

import data.local.*
import java.util.*

enum class TimingMethod(val apiString: String) {
    RealTime("realtime"),
    RealTimeNoLoads("realtime_noloads"),
    InGame("ingame")
}

// TODO look into supporting levels https://github.com/speedruncomorg/api/blob/master/version1/levels.md
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
    val defaultTimingMethod: TimingMethod,
    val isROMHack: Boolean,

    val gameTypeIds: List<GameTypeId>,
    val platformIds: List<PlatformId>,
    val regionIds: List<RegionId>,
    val genreIds: List<GenreId>,
    val engineIds: List<EngineId>,
    val developerIds: List<DeveloperId>,
    val publisherIds: List<PublisherId>,

    val moderators: List<User>,
    val categories : List<Category>,

    val weblink: String
)