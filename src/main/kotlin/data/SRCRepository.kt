package data

import data.local.GameId
import data.local.GamesDAO
import data.local.entities.Run
import data.local.entities.VariableAndValueIds
import data.local.entities.utils.RunSortDirection
import data.local.entities.utils.RunSortDiscriminator
import data.local.entities.utils.RunSortParameters
import data.remote.SRCService
import data.remote.responses.GameResponse
import data.remote.responses.RunResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import persistence.database.Game
import persistence.database.Settings
import java.util.*

class SRCRepository(
    private val gamesDAO: GamesDAO,
    private val srcService: SRCService
) {
    //
    private var cachedRuns: List<Run> = emptyList()

    fun getGames(query: String) = gamesDAO.getGames(query)

    fun getFullGame(gameId: GameId) =
        flow {
            cacheRuns(gameId)
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

    private suspend fun cacheRuns(gameId: GameId) {
        val runs = mutableListOf<Run>()
        var currentOffset = 0
        do {
            val responses = coroutineScope {
                val requests = Array(SRCService.PARALLEL_REQUESTS) { i ->
                    async {
                        srcService.fetchRuns(
                            gameId = gameId.value,
                            offset = currentOffset + i * SRCService.PAGINATION_MAX
                        )
                    }
                }
                awaitAll(*requests)
            }
            runs.addAll(responses.flatMap { it.runResponses }.map(RunResponse::toRun))

            currentOffset += SRCService.PAGINATION_MAX * SRCService.PARALLEL_REQUESTS
        } while (responses.none { it.pagination.size < it.pagination.max || it.pagination.size == 0 })

        cachedRuns = runs
    }

    suspend fun getCachedRuns(
        settings: Settings
    ) = withContext(Dispatchers.Default) {
        cachedRuns.filter { run ->
            (settings.runStatus?.let { run.runStatus == it } ?: true)
                    && (settings.categoryId?.let { run.categoryId == it } ?: true)
                    && settings.variablesAndValuesIds.filter(run)
        }.sortedWith { run1, run2 ->
            // TODO the main shortcoming is that stuff is compared by their id instead of their visible name
            when (settings.runSortDiscriminator) {
                RunSortDiscriminator.Game -> run1.gameId.value.compareTo(run2.gameId.value)
                RunSortDiscriminator.Category -> run1.categoryId.value.compareTo(run2.categoryId.value)
                RunSortDiscriminator.Level -> (run1.levelId?.value ?: "").compareTo(run2.levelId?.value ?: "")
                RunSortDiscriminator.Platform -> (run1.platformId?.value ?: "").compareTo(run2.platformId?.value ?: "")
                RunSortDiscriminator.Region -> (run1.regionId?.value ?: "").compareTo(run2.regionId?.value ?: "")
                RunSortDiscriminator.IsEmulated -> run1.isEmulated.compareTo(run2.isEmulated)
                RunSortDiscriminator.PrimaryTime -> run1.primaryTime.compareTo(run2.primaryTime)
                RunSortDiscriminator.RunDate -> (run1.runDate ?: Date(0)).compareTo(run2.runDate ?: Date(0))
                RunSortDiscriminator.SubmissionDate -> (run1.submissionDate ?: Date(0))
                    .compareTo(run2.submissionDate ?: Date(0))
                RunSortDiscriminator.VerificationDate -> (run1.verificationDate ?: Date(0))
                    .compareTo(run2.verificationDate ?: Date(0))
                RunSortDiscriminator.Status -> run1.runStatus.compareTo(run2.runStatus)
            }.times(
                when (settings.runSortDirection) {
                    RunSortDirection.Ascending -> 1
                    RunSortDirection.Descending -> -1
                }
            )
        }
    }

    private fun List<VariableAndValueIds>.filter(run: Run) = all { (filterVariableId, filterValueId) ->
        run.variablesAndValues.any { (runVariableId, runValueId) ->
            runVariableId == filterVariableId && runValueId == filterValueId
        }
    }

    fun getRuns(
        gameId: GameId,
        filters: Settings,
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