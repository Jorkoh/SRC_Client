package data.remote.utils

data class RunSortParameters(
    val discriminator: RunSortDiscriminator,
    val direction: RunSortDirection
)

enum class RunSortDiscriminator(val apiString: String) {
    Game("game"),
    Category("category"),
    Level("level"),
    Platform("platform"),
    Region("region"),
    IsEmulated("emulated"),
    RunDate("date"),
    SubmissionDate("submitted"),
    VerificationDate("verify-data"),
    Status("status")
}

enum class RunSortDirection(val apiString: String) {
    Ascending("asc"),
    Descending("desc")
}
