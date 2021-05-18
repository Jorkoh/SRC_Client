package data.local.entities

import data.local.UserId

enum class Role(val apiString: String) {
    Banned("banned"),
    User("user"),
    Trusted("trusted"),
    Moderator("moderator"),
    Admin("admin"),
    Programmer("programmer")
}

interface User{
    val name : String
    val countryCode : String?
}

data class RegisteredUser(
    val userId: UserId,
    override val name: String,
    val role: Role?,
    override val countryCode : String?,
    val weblink : String,
) : User

data class Guest(
    override val name: String,
    override val countryCode : String?,
) : User