package data

import data.local.GameId
import data.local.GamesDAO
import data.local.entities.Run
import data.local.entities.Status
import data.remote.SRCService
import data.remote.utils.RunSortParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
                        games.addAll(bulkGameResponses.map { it.toGame() })
                    }
                    emit("Downloaded ${games.size} games...")
                } while (response.pagination.size == response.pagination.max)
                emit("Storing ${games.size} games...")
                gamesDAO.insertGames(games)
            }
        }.flowOn(Dispatchers.IO)

    fun getRuns(
        gameId: GameId,
        status: Status,
        sortingParams: RunSortParameters? = null
    ) = flow {
        val runs = mutableListOf<Run>()
        do {
            val response = srcService.fetchRuns(
                gameId = gameId.value,
                status = status.apiString,
                orderBy = sortingParams?.discriminator?.apiString,
                direction = sortingParams?.direction?.apiString,
                offset = runs.size
            ).apply {
                runs.addAll(runResponses.map { it.toRun() })
            }
        } while (response.pagination.size == response.pagination.max)
        emit(runs)
    }.flowOn(Dispatchers.IO)
}