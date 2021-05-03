package data

import data.local.*
import data.local.entities.Guest
import data.local.entities.RegisteredUser
import data.local.entities.Run
import data.remote.responses.*
import persistence.database.Game

fun BulkGameResponse.toGame() = Game(GameId(id), abbreviation, names.international)

fun RunResponse.toRun() = Run(
    runId = RunId(id),
    gameId = GameId(gameId),
    categoryId = CategoryId(categoryId),
    levelId = if (levelId != null) LevelId(levelId) else null,
    variables = variables.values,
    status = status.value,
    verifierId = if (status.verifierId != null) UserId(status.verifierId) else null,
    verificationDate = status.verificationDate,
    players = players.players.map(PlayerResponse::toUser),
    comment = comment,
    runDate = runDate,
    submissionDate = submissionDate,
    timePrimary = times.primaryT,
    timeReal = times.realtimeT,
    timeRealNoLoads = times.realtimeNoloadsT,
    timeIngame = times.ingameT,
    videoText = videos?.text,
    videoLinks = videos?.links?.map { it.uri } ?: emptyList(),
    weblink = weblink
)

fun PlayerResponse.toUser() = when (this) {
    is GuestResponse -> Guest(name)
    is UserResponse -> RegisteredUser(
        userId = UserId(playerId),
        name = names.international,
        role = role,
        countryCode = location?.country?.code,
        weblink = weblink
    )
}