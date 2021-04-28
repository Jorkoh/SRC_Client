package data.local

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import settings.database.Game
import settings.database.Settings
import settings.database.SettingsDB
import settings.database.SettingsQueries

inline class SettingsId(val value: Long)
inline class GameId(val value: String)
inline class RunId(val value: String)

class SettingsRepository {

    private val database: SettingsDB

    init {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY + "db")
        val version = driver.getVersion()
        if (version == 0) {
            SettingsDB.Schema.create(driver)
            driver.setVersion(1)
        }

        val gameIdAdapter = object : ColumnAdapter<GameId, String> {
            override fun decode(databaseValue: String) = GameId(databaseValue)
            override fun encode(value: GameId) = value.value
        }
        val settingsIdAdapter = object : ColumnAdapter<SettingsId, Long> {
            override fun decode(databaseValue: Long) = SettingsId(databaseValue)
            override fun encode(value: SettingsId) = value.value
        }
        database = SettingsDB(
            driver = driver,
            gameAdapter = Game.Adapter(gameIdAdapter),
            settingsAdapter = Settings.Adapter(settingsIdAdapter, gameIdAdapter)
        )
    }

    private val settingsQueries: SettingsQueries = database.settingsQueries

    fun getSelectedGame() = settingsQueries.getSelectedGame().asFlow().mapToOneOrNull()

    fun setSelectedGame(newSelectedGame: Game?) {
        settingsQueries.transaction {
            if (newSelectedGame != null) {
                settingsQueries.insertGame(newSelectedGame)
            }
            settingsQueries.updateSelectedGameId(newSelectedGame?.id)
        }
    }

    private fun JdbcSqliteDriver.getVersion(): Int {
        executeQuery(null, "PRAGMA user_version;", 0, null).apply {
            val version = getLong(0)?.toInt() ?: 0
            // executeQuery doesn't auto-close like execute for some reason
            close()
            return version
        }
    }

    private fun JdbcSqliteDriver.setVersion(version: Int) {
        execute(null, "PRAGMA user_version = $version;", 0, null)
    }
}