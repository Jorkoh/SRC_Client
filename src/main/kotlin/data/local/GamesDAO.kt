package data.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import persistence.database.DatabaseQueries
import persistence.database.Game

class GamesDAO(databaseSingleton: DatabaseSingleton) {

    private val queries: DatabaseQueries = databaseSingleton.db.databaseQueries

    fun getGames(query: String?, maxCount: Int = 10): Flow<List<Game>> {
        return if (query.isNullOrEmpty()) {
            queries.getAllGames().asFlow().mapToList()
        } else {
            queries.getAllGamesFiltered(
                nameMatch = query.replace(" ", "%"),
                limit = maxCount.toLong()
            ).asFlow().mapToList()
        }
    }

    fun insertGames(games: List<Game>, keepOldSelected: Boolean = false) {
        queries.transaction {
            val previousId = queries.getSelectedGame().executeAsOneOrNull()?.id
            queries.deleteGames()
            games.forEach { queries.insertGame(it) }
            queries.updateSelectedGameId(
                // previous, default or w/e is first
                if (keepOldSelected && previousId != null && games.any { it.id == previousId }) {
                    previousId
                } else if (games.any { it.id == GameId.Default }) {
                    GameId.Default
                } else {
                    games.first().id
                }
            )
        }
    }

    fun hasGameCache() = queries.getGameCount().executeAsOne() > 0

    fun getSelectedGameBlocking() = queries.getSelectedGame().executeAsOne()

    fun getSelectedGame() = queries.getSelectedGame().asFlow().mapToOne()

    fun setSelectedGameIfChanged(newSelectedGame: Game) {
        queries.transaction {
            val previousGame = queries.getSelectedGame().executeAsOne()
            if (previousGame != newSelectedGame) {
                queries.updateSelectedGameId(newSelectedGame.id)
            }
        }
    }
}