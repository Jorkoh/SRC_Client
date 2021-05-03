package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaginatedBulkGamesResponse(
    @Json(name = "data")
    val bulkGameResponses: List<BulkGameResponse>,
    @Json(name = "pagination")
    val pagination: Pagination
)

@JsonClass(generateAdapter = true)
data class BulkGameResponse(
    @Json(name = "abbreviation")
    val abbreviation: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "names")
    val names: Names,
    @Json(name = "weblink")
    val weblink: String
)