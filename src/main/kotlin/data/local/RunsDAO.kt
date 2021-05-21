package data.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.flow.map
import persistence.database.DatabaseQueries

class RunsDAO(databaseSingleton: DatabaseSingleton) {

    private val queries: DatabaseQueries = databaseSingleton.db.databaseQueries

    fun getSelectedRunId() = queries.getSelectedRunId().asFlow().mapToOne().map { it.selectedRunId }

    fun selectRun(runId: RunId) = queries.updateSelectedRunId(runId)

    fun deselectRun() = queries.updateSelectedRunId(null)
}