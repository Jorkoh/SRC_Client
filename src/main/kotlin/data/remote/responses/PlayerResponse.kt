package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class PlayerType(val apiString: String) {
    User("user"),
    Guest("guest")
}

sealed class PlayerResponse(
    @Json(name = "rel")
    val playerType: PlayerType,
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id")
    val playerId: String,
    @Json(name = "weblink")
    val weblink: String,
    @Json(name = "names")
    val names: Names,
    @Json(name = "links")
    val links: List<Link>,
    @Json(name = "role")
    val role: String
) : PlayerResponse(PlayerType.User)

@JsonClass(generateAdapter = true)
data class GuestResponse(
    @Json(name = "name")
    val name: String,
    @Json(name = "links")
    val links: List<Link>,
) : PlayerResponse(PlayerType.Guest)