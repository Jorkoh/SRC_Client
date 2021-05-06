package data.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import persistence.database.DatabaseQueries
import persistence.database.Game

class FiltersDAO(databaseSingleton: DatabaseSingleton) {
    private val queries: DatabaseQueries = databaseSingleton.db.databaseQueries

    fun getSelectedGameBlocking() = queries.getSelectedGame().executeAsOne()

    fun getSelectedGame() = queries.getSelectedGame().asFlow().mapToOne()

    fun setSelectedGameIfChanged(newSelectedGame: Game?) {
        queries.transaction {
            val previousGame = queries.getSelectedGame().executeAsOne()
            if (previousGame != newSelectedGame) {
                queries.updateSelectedGameId(newSelectedGame?.id)
            }
        }
    }
}