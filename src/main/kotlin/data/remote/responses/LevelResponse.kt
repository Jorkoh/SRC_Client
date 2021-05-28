package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LevelResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "weblink")
    val weblink: String,
    @Json(name = "rules")
    val rules: String?,
    @Json(name = "links")
    val links: List<Link>,
)