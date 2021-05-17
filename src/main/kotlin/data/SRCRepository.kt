package data

import data.local.GameId
import data.local.GamesDAO
import data.local.entities.Run
import data.remote.SRCService
import data.remote.responses.GameResponse
import data.remote.responses.RunResponse
import data.remote.utils.RunSortParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import persistence.database.Filters
import persistence.database.Game

class SRCRepository(
    private val gamesDAO: GamesDAO,
    private val srcService: SRCService
) {
    fun getGames(query: String) = gamesDAO.getGames(query)

    fun getFullGame(gameId: GameId) =
        flow {
            val fullGame = srcService.fetchFullGame(
                gameId = gameId.value
            ).fullGameResponse.toFullGame()
            emit(fullGame)
        }

    fun cacheGamesIfNeeded(forceDownload: Boolean = false) =
        flow {
            if (forceDownload || !gamesDAO.hasGameCache()) {
                emit("Downloading games...")
                val games = mutableListOf<Game>()
                var currentOffset = 0
                do {
                    val responses = coroutineScope {
                        val requests = Array(SRCService.PARALLEL_REQUESTS) { i ->
                            async {
                                srcService.fetchGames(offset = currentOffset + i * SRCService.PAGINATION_MAX_BULK_MODE)
                            }
                        }
                        awaitAll(*requests)
                    }

                    responses.flatMap { it.gameResponses }.apply {
                        games.addAll(map(GameResponse::toGame))
                    }
                    emit("Downloaded ${games.size} games...")
                    currentOffset += SRCService.PAGINATION_MAX_BULK_MODE * SRCService.PARALLEL_REQUESTS
                } while (responses.none { it.pagination.size < it.pagination.max || it.pagination.size == 0 })
                emit("Storing ${games.size} games...")
                gamesDAO.insertGames(games, forceDownload)
            }
        }.flowOn(Dispatchers.IO)


    fun getRuns(
        gameId: GameId,
        filters: Filters,
        sortingParams: RunSortParameters? = null
    ) = flow {
        val runs = mutableListOf<Run>()
        var currentOffset = 0
        do {
            val responses = coroutineScope {
                val requests = Array(SRCService.PARALLEL_REQUESTS) { i ->
                    async {
                        srcService.fetchRuns(
                            gameId = gameId.value,
                            categoryId = filters.categoryId?.value,
                            status = filters.runStatus?.apiString,
                            orderBy = sortingParams?.discriminator?.apiString,
                            direction = sortingParams?.direction?.apiString,
                            offset = currentOffset + i * SRCService.PAGINATION_MAX
                        )
                    }
                }
                awaitAll(*requests)
            }

            responses.flatMap { it.runResponses }.apply {
                val filteredRunResponses = filter { response ->
                    // A response needs to match all defined custom variable filters
                    filters.variablesAndValuesIds.all { (filterVariableId, filterValueId) ->
                        response.variablesAndValues.variablesAndValues.any { (responseVariableId, responseValueId) ->
                            responseVariableId == filterVariableId.value && responseValueId == filterValueId.value
                        }
                    }
                }
                runs.addAll(filteredRunResponses.map(RunResponse::toRun))
            }
            currentOffset += SRCService.PAGINATION_MAX * SRCService.PARALLEL_REQUESTS
        } while (responses.none { it.pagination.size < it.pagination.max || it.pagination.size == 0 })
        emit(runs)
    }.flowOn(Dispatchers.IO)
}