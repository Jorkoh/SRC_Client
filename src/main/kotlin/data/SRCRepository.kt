package data

import data.local.GameId
import data.local.GamesDAO
import data.local.entities.Run
import data.remote.SRCService
import data.remote.responses.RunResponse
import data.remote.utils.RunSortParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import persistence.database.Filters
import persistence.database.Game

class SRCRepository(
    private val gamesDAO: GamesDAO,
    private val srcService: SRCService
) {
    fun getGames(query: String) = gamesDAO.getGames(query)

    fun cacheGamesIfNeeded(forceDownload: Boolean = false) =
        flow {
            if (forceDownload || !gamesDAO.hasGameCache()) {
                emit("Downloading games...")
                val games = mutableListOf<Game>()
                do {
                    val response = srcService.fetchGames(offset = games.size).apply {
                        games.addAll(gameResponses.map { it.toGame() })
                    }
                    emit("Downloaded ${games.size} games...")
                } while (response.pagination.size == response.pagination.max)
                emit("Storing ${games.size} games...")
                // TODO whenever the force update for game list is added previous value should stay selected
                gamesDAO.insertGames(games)
            }
        }.flowOn(Dispatchers.IO)

    fun getFullGame(gameId: GameId) =
        flow {
            val fullGame = srcService.fetchFullGame(
                gameId = gameId.value
            ).fullGameResponse.toFullGame()
            emit(fullGame)
        }

    fun getRuns(
        gameId: GameId,
        filters: Filters,
        sortingParams: RunSortParameters? = null
    ) = flow {
        val runs = mutableListOf<Run>()
        var offset = 0
        do {
            val response = srcService.fetchRuns(
                gameId = gameId.value,
                categoryId = filters.categoryId?.value,
                status = filters.runStatus?.apiString,
                orderBy = sortingParams?.discriminator?.apiString,
                direction = sortingParams?.direction?.apiString,
                offset = offset
            ).apply {
                offset += runResponses.size
                val filteredRunResponses = runResponses.filter { response ->
                    // A response needs to match all defined custom variable filters
                    filters.variablesAndValuesIds.all { (filterVariableId, filterValueId) ->
                        response.variablesAndValues.variablesAndValues.any { (responseVariableId, responseValueId) ->
                            responseVariableId == filterVariableId.value && responseValueId == filterValueId.value
                        }
                    }
                }
                runs.addAll(filteredRunResponses.map(RunResponse::toRun))
            }
        } while (response.pagination.size == response.pagination.max)
        emit(runs)
    }.flowOn(Dispatchers.IO)
}