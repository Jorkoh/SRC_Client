package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import data.local.entities.Status
import java.util.*

@JsonClass(generateAdapter = true)
data class PaginatedRunsResponse(
    @Json(name = "data")
    val runResponses: List<RunResponse>,
    @Json(name = "pagination")
    val pagination: Pagination
)

@JsonClass(generateAdapter = true)
data class RunResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "status")
    val status: RunStatus,
    @Json(name = "weblink")
    val weblink: String,

    @Json(name = "game")
    val gameId: String,
    @Json(name = "category")
    val categoryId: String,
    @Json(name = "level")
    val levelId: String?,
    @Json(name = "values")
    val variablesAndValues: VariablesAndValuesResponses,

    @Json(name = "players")
    val players: Players,

    @Json(name = "comment")
    val comment: String?,
    @Json(name = "date")
    val runDate: Date,
    @Json(name = "links")
    val links: List<Link>,
    @Json(name = "splits")
    val splits: Link?,
    @Json(name = "submitted")
    val submissionDate: Date?,
    @Json(name = "system")
    val system: GameSystem,
    @Json(name = "times")
    val times: Times,
    @Json(name = "videos")
    val videos: Videos?,
)

@JsonClass(generateAdapter = true)
data class Players(
    @Json(name = "data")
    val players: List<PlayerResponse>
)

@JsonClass(generateAdapter = true)
data class RunStatus(
    @Json(name = "status")
    val value: Status,
    @Json(name = "examiner")
    val verifierId: String?,
    @Json(name = "verify-date")
    val verificationDate: Date?,
)

@JsonClass(generateAdapter = true)
data class GameSystem(
    @Json(name = "emulated")
    val emulated: Boolean,
    @Json(name = "platform")
    val platform: String,
    @Json(name = "region")
    val regionId: String?
)

@JsonClass(generateAdapter = true)
data class Times(
    @Json(name = "primary")
    val primary: String,
    @Json(name = "primary_t")
    val primarySeconds: Double,
    @Json(name = "realtime")
    val realTime: String?,
    @Json(name = "realtime_t")
    val realTimeSeconds: Double?,
    @Json(name = "realtime_noloads")
    val realTimeNoLoads: String?,
    @Json(name = "realtime_noloads_t")
    val realTimeNoLoadsSeconds: Double?,
    @Json(name = "ingame")
    val inGame: String?,
    @Json(name = "ingame_t")
    val inGameSeconds: Double?,
)

data class VariablesAndValuesResponses(
    val variablesAndValues: List<VariableAndValueResponse>
)

data class VariableAndValueResponse(
    val variableId: String,
    val valueId: String
)

@JsonClass(generateAdapter = true)
data class Videos(
    @Json(name = "text")
    val text: String?,
    @Json(name = "links")
    val links: List<Link>?
)