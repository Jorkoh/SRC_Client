package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import data.local.entities.Role

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
    @Json(name = "location")
    val location: Location?,
    @Json(name = "role")
    val role: Role
) : PlayerResponse(PlayerType.User)

@JsonClass(generateAdapter = true)
data class GuestResponse(
    @Json(name = "name")
    val name: String
) : PlayerResponse(PlayerType.Guest)

@JsonClass(generateAdapter = true)
data class Location(
    @Json(name = "country")
    val country: Country
)

@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "code")
    val code: String
)