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

    fun insertGames(games: List<Game>) {
        queries.transaction {
            games.forEach { queries.insertGame(it) }

            // Set Minecraft as default, if not found w/e is first
            queries.updateSelectedGameId(
                games.firstOrNull {
                    it.id == GameId("j1npme6p")
                }?.id ?: games.first().id
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