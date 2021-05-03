package data.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import persistence.database.DatabaseQueries
import persistence.database.Game

class SettingsDAO(databaseSingleton: DatabaseSingleton) {
    private val queries: DatabaseQueries = databaseSingleton.db.databaseQueries

    fun getSelectedGame() = queries.getSelectedGame().asFlow().mapToOneOrNull()

    fun setSelectedGame(newSelectedGame: Game?) {
        queries.transaction {
            if (newSelectedGame != null) {
                queries.insertGame(newSelectedGame)
            }
            queries.updateSelectedGameId(newSelectedGame?.id)
        }
    }
}