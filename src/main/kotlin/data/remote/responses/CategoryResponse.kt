package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import data.local.entities.CategoryType
import data.local.entities.PlayerCountType

@JsonClass(generateAdapter = true)
data class CategoryResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "weblink")
    val weblink: String,

    @Json(name = "type")
    val type: CategoryType,
    @Json(name = "rules")
    val rules: String?,
    @Json(name = "players")
    val playerCount: PlayerCount,
    @Json(name = "miscellaneous")
    val isMiscellaneous: Boolean,
    @Json(name = "links")
    val links: List<Link>,

    // THIS PROPERTY REQUIRES THE EMBED PARAMETER IN REQUEST
    @Json(name = "variables")
    val variables : Variables
)

@JsonClass(generateAdapter = true)
data class PlayerCount(
    @Json(name = "type")
    val type: PlayerCountType,
    @Json(name = "value")
    val value: Int
)

@JsonClass(generateAdapter = true)
data class Variables(
    @Json(name = "data")
    val values : List<VariableResponse>
)