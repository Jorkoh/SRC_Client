package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import data.local.entities.TimingMethod
import java.util.*

@JsonClass(generateAdapter = true)
data class PaginatedBulkGamesResponse(
    @Json(name = "data")
    val gameResponses: List<GameResponse>,
    @Json(name = "pagination")
    val pagination: Pagination
)

@JsonClass(generateAdapter = true)
data class GameResponse(
    @Json(name = "abbreviation")
    val abbreviation: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "names")
    val names: Names,
    @Json(name = "weblink")
    val weblink: String
)

@JsonClass(generateAdapter = true)
data class PaginatedFullGameResponse(
    @Json(name = "data")
    val fullGameResponse: FullGameResponse
)

@JsonClass(generateAdapter = true)
data class FullGameResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "abbreviation")
    val abbreviation: String,
    @Json(name = "names")
    val names: Names,
    @Json(name = "weblink")
    val weblink: String,

    @Json(name = "release-date")
    val releaseDate: Date,
    @Json(name = "created")
    val additionDate: Date?,

    @Json(name = "ruleset")
    val ruleset: Ruleset,
    @Json(name = "romhack")
    val isROMHack : Boolean,

    @Json(name = "gametypes")
    val gameTypeIds : List<String>,
    @Json(name = "platforms")
    val platformIds : List<String>,
    @Json(name = "regions")
    val regionIds : List<String>,
    @Json(name = "genres")
    val genreIds : List<String>,
    @Json(name = "engines")
    val engineIds : List<String>,
    @Json(name = "developers")
    val developerIds : List<String>,
    @Json(name = "publishers")
    val publisherIds : List<String>,

    @Json(name = "assets")
    val assets : Assets,
    @Json(name = "links")
    val links : List<Link>,

    // THIS PROPERTIES REQUIRE THE EMBED PARAMETER IN REQUEST
    @Json(name = "moderators")
    val moderators : Moderators, // replaces the original moderators type in the response
    @Json(name = "levels")
    val levels : Levels,
    @Json(name = "categories")
    val categories : Categories
)

@JsonClass(generateAdapter = true)
data class Ruleset(
    @Json(name = "show-milliseconds")
    val showMilliseconds: Boolean,
    @Json(name = "require-verification")
    val requireVerification: Boolean,
    @Json(name = "require-video")
    val requireVideo: Boolean,

    @Json(name = "default-time")
    val defaultTimingMethod: TimingMethod,
    @Json(name = "emulators-allowed")
    val emulatorsAllowed: Boolean,
    @Json(name = "run-times")
    val runTimes: List<TimingMethod>,
)

@JsonClass(generateAdapter = true)
data class Assets(
    @Json(name = "logo")
    val logo : Asset,
    @Json(name = "cover-tiny")
    val coverTiny : Asset,
    @Json(name = "cover-small")
    val coverSmall : Asset,
    @Json(name = "cover-medium")
    val coverMedium : Asset,
    @Json(name = "cover-large")
    val coverLarge : Asset,
    @Json(name = "icon")
    val icon : Asset,
    @Json(name = "trophy-1st")
    val trophyFirst: Asset,
    @Json(name = "trophy-2nd")
    val trophySecond: Asset,
    @Json(name = "trophy-3rd")
    val trophyThird: Asset,
    @Json(name = "trophy-4th")
    val trophyFourth: Asset?,
    @Json(name = "background")
    val background: Asset?,
    @Json(name = "foreground")
    val foreground: Asset?
)

@JsonClass(generateAdapter = true)
data class Asset(
    @Json(name = "uri")
    val uri : String,
    @Json(name = "width")
    val width : Int,
    @Json(name = "height")
    val height : Int
)

@JsonClass(generateAdapter = true)
data class Moderators(
    @Json(name = "data")
    val values : List<UserResponse>
)

@JsonClass(generateAdapter = true)
data class Levels(
    @Json(name = "data")
    val values : List<LevelResponse>
)

@JsonClass(generateAdapter = true)
data class Categories(
    @Json(name = "data")
    val values : List<CategoryResponse>
)