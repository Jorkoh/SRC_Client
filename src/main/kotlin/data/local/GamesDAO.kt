package data.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
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
        }
    }

    fun hasGameCache() = queries.getGameCount().executeAsOne() > 0
}