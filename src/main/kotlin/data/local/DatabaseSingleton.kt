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
import persistence.database.SelectedEntities
import persistence.database.Settings

@JvmInline
value class GameId(val value: String) {
    companion object {
        val Default = GameId("j1npme6p") // Minecraft: Java Edition
    }
}
@JvmInline
value class CategoryId(val value: String)

@Serializable
@JvmInline
value class VariableId(val value: String)

@Serializable
@JvmInline
value class ValueId(val value: String)
@JvmInline
value class LevelId(val value: String)
@JvmInline
value class RunId(val value: String)
@JvmInline
value class UserId(val value: String)
@JvmInline
value class GameTypeId(val value: String)
@JvmInline
value class PlatformId(val value: String)
@JvmInline
value class RegionId(val value: String)
@JvmInline
value class GenreId(val value: String)
@JvmInline
value class EngineId(val value: String)
@JvmInline
value class DeveloperId(val value: String)
@JvmInline
value class PublisherId(val value: String)

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
        val runIdAdapter = object : ColumnAdapter<RunId, String> {
            override fun decode(databaseValue: String) = RunId(databaseValue)
            override fun encode(value: RunId) = value.value
        }
        val userIdAdapter = object : ColumnAdapter<UserId, String> {
            override fun decode(databaseValue: String) = UserId(databaseValue)
            override fun encode(value: UserId) = value.value
        }
        val levelIdAdapter = object : ColumnAdapter<LevelId, String> {
            override fun decode(databaseValue: String) = LevelId(databaseValue)
            override fun encode(value: LevelId) = value.value
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
            settingsAdapter = Settings.Adapter(
                searchQueryTargetAdapter = EnumColumnAdapter(),
                runStatusAdapter = EnumColumnAdapter(),
                verifierIdAdapter = userIdAdapter,
                leaderboardTypeAdapter = EnumColumnAdapter(),
                levelIdAdapter = levelIdAdapter,
                categoryIdAdapter = categoryIdAdapter,
                leaderboardStyleAdapter = EnumColumnAdapter(),
                variablesAndValuesIdsAdapter = variablesAndValuesIdsAdapter,
                runSortDiscriminatorAdapter = EnumColumnAdapter(),
                runSortDirectionAdapter = EnumColumnAdapter()
            ),
            selectedEntitiesAdapter = SelectedEntities.Adapter(
                selectedGameIdAdapter = gameIdAdapter,
                selectedRunIdAdapter = runIdAdapter
            ),
            gameAdapter = Game.Adapter(
                idAdapter = gameIdAdapter
            ),
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