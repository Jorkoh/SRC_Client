package data

import data.local.GameId
import data.local.GamesDAO
import data.local.RunId
import data.local.entities.*
import data.remote.SRCService
import data.remote.responses.GameResponse
import data.remote.responses.RunResponse
import data.utils.LeaderboardStyle
import data.utils.RunSortDirection
import data.utils.RunSortDiscriminator
import data.utils.RunSortParameters
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
    private var cachedRuns: List<Run> = emptyList()

    fun getGames(query: String) = gamesDAO.getGames(query)

    fun getFullGame(gameId: GameId) = flow {
        cacheRuns(gameId)
        val fullGame = srcService.fetchFullGame(gameId = gameId.value).fullGameResponse.toFullGame()
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
            (settings.runStatus?.filter(run) ?: true)
                    && (settings.categoryId?.let { run.categoryId == it } ?: true)
                    && settings.variablesAndValuesIds.filter(run)
                    && settings.filterQuery.filter(run)
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
        }.run {
            if (settings.leaderboardStyle == LeaderboardStyle.Yes) {
                /*
                This eats runs with no players which is ok. It also eats runs where a guest
                has the name of a user id. Don't think that's a real problem tho
                 */
                distinctBy {
                    when (val player = it.players.firstOrNull()) {
                        is RegisteredUser -> player.userId
                        is Guest -> player.name
                        else -> null
                    }
                }
            } else {
                this
            }
        }
    }

    private fun RunStatus.filter(run: Run) = if (this == RunStatus.PendingPlusApproved) {
        run.runStatus == RunStatus.Pending || run.runStatus == RunStatus.Approved
    } else {
        run.runStatus == this
    }

    private fun List<VariableAndValueIds>.filter(run: Run) = all { (filterVariableId, filterValueId) ->
        run.variablesAndValuesIds.any { (runVariableId, runValueId) ->
            runVariableId == filterVariableId && runValueId == filterValueId
        }
    }

    private fun String.filter(run: Run): Boolean {
        return if (this.isNotBlank()) {
            run.players.joinToString(" ") { it.name }.contains(other = this, ignoreCase = true)
                    || run.comment?.contains(other = this, ignoreCase = true) ?: false
                    || run.rejectionReason?.contains(other = this, ignoreCase = true) ?: false
        } else {
            true
        }
    }

    fun getFullRun(runId: RunId) = flow {
        val fullRun = srcService.fetchRun(runId = runId.value).fullRunResponse.toFullRun()
        emit(fullRun)
    }
}