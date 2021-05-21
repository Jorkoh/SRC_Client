package data.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import persistence.database.DatabaseQueries
import persistence.database.Settings

class SettingsDAO(databaseSingleton: DatabaseSingleton) {
    private val queries: DatabaseQueries = databaseSingleton.db.databaseQueries

    fun getSettings() = queries.getSettings().asFlow().mapToOne()

    fun setSettings(newSettings: Settings) {
        queries.transaction {
            queries.deleteSettings()
            queries.insertSettings(newSettings)
        }
    }

    fun resetGameSpecificSettings() {
        queries.resetGameSpecificSettings()
    }
}