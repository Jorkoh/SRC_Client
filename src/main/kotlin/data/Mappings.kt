package data

import data.local.*
import data.local.entities.*
import data.remote.responses.*
import persistence.database.Game
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun GameResponse.toGame() = Game(GameId(id), abbreviation, names.international, false)

// assets, links and levels not mapped for now
fun FullGameResponse.toFullGame() = FullGame(
    gameId = GameId(id),
    name = names.international,
    abbreviation = abbreviation,
    releaseDate = releaseDate,
    additionDate = additionDate,
    showMilliseconds = ruleset.showMilliseconds,
    requireVerification = ruleset.requireVerification,
    requireVideo = ruleset.requireVideo,
    emulatorsAllowed = ruleset.emulatorsAllowed,
    timingMethods = ruleset.runTimes,
    defaultTimingMethod = ruleset.defaultTimingMethod,
    isROMHack = isROMHack,
    gameTypeIds = gameTypeIds.map(::GameTypeId),
    platformIds = platformIds.map(::PlatformId),
    regionIds = regionIds.map(::RegionId),
    genreIds = genreIds.map(::GenreId),
    engineIds = engineIds.map(::EngineId),
    developerIds = developerIds.map(::DeveloperId),
    publisherIds = publisherIds.map(::PublisherId),
    moderators = moderators.values.map(PlayerResponse::toUser),
    categories = categories.values.map(CategoryResponse::toCategory),
    weblink = weblink
)

// links not mapped for now
fun CategoryResponse.toCategory() = Category(
    categoryId = CategoryId(id),
    name = name,
    type = type,
    rules = rules,
    playerCountType = playerCount.type,
    playerCount = playerCount.value,
    isMiscellaneous = isMiscellaneous,
    variables = variables.values.map(VariableResponse::toVariable),
    weblink = weblink
)

// links not mapped for now, level scope not mapped for now
fun VariableResponse.toVariable() = Variable(
    variableId = VariableId(id),
    name = name,
    categoryId = categoryId?.let(::CategoryId),
    scope = scope.value,
    isMandatory = mandatory,
    isUserDefined = userDefined,
    isSubCategory = isSubCategory,
    obsoletes = obsoletes,
    values = valueResponses.valueResponses.map(ValueResponse::toValue),
    defaultValueId = valueResponses.defaultValueId?.let(::ValueId)
)

fun ValueResponse.toValue() = Value(
    valueId = ValueId(id),
    label = label,
    rules = rules,
    miscellaneousFlag = miscellaneousFlag
)

fun RunResponse.toRun() = Run(
    runId = RunId(id),
    gameId = GameId(gameId),
    categoryId = CategoryId(categoryId),
    levelId = levelId?.let(::LevelId),
    variablesAndValues = variablesAndValues.variablesAndValues.map {
        VariableAndValueIds(VariableId(it.variableId), ValueId(it.valueId))
    },
    isEmulated = system.emulated,
    platformId = system.platformId?.let(::PlatformId),
    regionId = system.regionId?.let(::RegionId),
    runStatus = status.value,
    verifierId = status.verifierId?.let(::UserId),
    verificationDate = status.verificationDate,
    players = players.players.map(PlayerResponse::toUser),
    comment = comment,
    runDate = runDate,
    submissionDate = submissionDate,
    primaryTime = times.primarySeconds.toDuration(DurationUnit.SECONDS),
    realTime = times.realTimeSeconds?.toDuration(DurationUnit.SECONDS),
    realTimeNoLoads = times.realTimeNoLoadsSeconds?.toDuration(DurationUnit.SECONDS),
    inGameTime = times.inGameSeconds?.toDuration(DurationUnit.SECONDS),
    videoText = videos?.text,
    videoLinks = videos?.links?.map { it.uri } ?: emptyList(),
    weblink = weblink
)

fun PlayerResponse.toUser() = when (this) {
    is GuestResponse -> {
        name.split(']').let { parts ->
            if (parts.size == 2) {
                Guest(name = parts[1], countryCode = parts[0].drop(1))
            } else {
                Guest(name = name, countryCode = null)
            }
        }
    }
    is UserResponse -> RegisteredUser(
        userId = UserId(playerId),
        name = names.international,
        role = role,
        countryCode = location?.country?.code,
        weblink = weblink
    )
}