package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Pagination(
    @Json(name = "links")
    val links: List<Link>,
    @Json(name = "max")
    val max: Int,
    @Json(name = "offset")
    val offset: Int,
    @Json(name = "size")
    val size: Int
)

@JsonClass(generateAdapter = true)
data class Link(
    @Json(name = "rel")
    val rel: String?,
    @Json(name = "uri")
    val uri: String
)

@JsonClass(generateAdapter = true)
data class Names(
    @Json(name = "international")
    val international: String,
    @Json(name = "japanese")
    val japanese: String?
)