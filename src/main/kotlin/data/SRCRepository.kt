package data

import data.local.GameId
import data.local.entities.Run
import data.local.entities.Status
import data.remote.SRCService
import data.remote.utils.RunSortParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SRCRepository(private val service: SRCService) {
    fun getGames(query: String) =
        flow {
            val games = service.fetchGames(query = query).gameResponses.map { it.toGame() }
            emit(games)
        }.flowOn(Dispatchers.IO)

    fun getRuns(
        gameId: GameId,
        status: Status,
        sortingParams: RunSortParameters? = null
    ) = flow {
        val runs = mutableListOf<Run>()
        do {
            val response = service.fetchRuns(
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