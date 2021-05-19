package data.local

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import data.local.entities.VariableAndValueIds
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import persistence.database.DatabaseInstance
import persistence.database.Game
import persistence.database.Settings

inline class SettingsId(val value: Long) {
    companion object {
        // can't override invoke and private constructor because sqldelight fails generation :(
        val Default = SettingsId(1)
    }
}

inline class GameId(val value: String) {
    companion object {
        val Default = GameId("j1npme6p") // Minecraft: Java Edition
    }
}

inline class CategoryId(val value: String)

@Serializable
inline class VariableId(val value: String)

@Serializable
inline class ValueId(val value: String)
inline class LevelId(val value: String)
inline class RunId(val value: String)
inline class UserId(val value: String)
inline class GameTypeId(val value: String)
inline class PlatformId(val value: String)
inline class RegionId(val value: String)
inline class GenreId(val value: String)
inline class EngineId(val value: String)
inline class DeveloperId(val value: String)
inline class PublisherId(val value: String)

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
            override fun decode(databaseValue: Long) = SettingsId.Default
            override fun encode(value: SettingsId) = value.value
        }
        val categoryIdAdapter = object : ColumnAdapter<CategoryId, String> {
            override fun decode(databaseValue: String) = CategoryId(databaseValue)
            override fun encode(value: CategoryId) = value.value
        }
        val variablesAndValuesIdsAdapter = object : ColumnAdapter<List<VariableAndValueIds>, String> {
            override fun decode(databaseValue: String) = Json.decodeFromString<List<VariableAndValueIds>>(databaseValue)
            override fun encode(value: List<VariableAndValueIds>) = Json.encodeToString(value)
        }

        db = DatabaseInstance(
            driver = driver,
            gameAdapter = Game.Adapter(
                idAdapter = gameIdAdapter
            ),
            settingsAdapter = Settings.Adapter(
                idAdapter = settingsIdAdapter,
                runStatusAdapter = EnumColumnAdapter(),
                categoryIdAdapter = categoryIdAdapter,
                leaderboardStyleAdapter = EnumColumnAdapter(),
                variablesAndValuesIdsAdapter = variablesAndValuesIdsAdapter,
                runSortDiscriminatorAdapter = EnumColumnAdapter(),
                runSortDirectionAdapter = EnumColumnAdapter()
            )
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