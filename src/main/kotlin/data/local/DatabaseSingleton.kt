package data.local

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import persistence.database.DatabaseInstance
import persistence.database.Game
import persistence.database.Settings

inline class SettingsId(val value: Long)
inline class GameId(val value: String)
inline class CategoryId(val value: String)
inline class LevelId(val value: String)
inline class RunId(val value: String)
inline class UserId(val value: String)

class DatabaseSingleton {
    val db: DatabaseInstance

    init {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY + "db")
        val version = driver.getVersion()
        if (version == 0) {
            DatabaseInstance.Schema.create(driver)
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
        db = DatabaseInstance(
            driver = driver,
            gameAdapter = Game.Adapter(gameIdAdapter),
            settingsAdapter = Settings.Adapter(settingsIdAdapter, gameIdAdapter)
        )
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