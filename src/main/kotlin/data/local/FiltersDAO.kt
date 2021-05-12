package data.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import data.local.entities.RunStatus
import persistence.database.DatabaseQueries
import persistence.database.Filters
import persistence.database.Game

class FiltersDAO(databaseSingleton: DatabaseSingleton) {
    private val queries: DatabaseQueries = databaseSingleton.db.databaseQueries

    fun getFilters() = queries.getFilters().asFlow().mapToOne()

    fun setFilters(newFilters: Filters) {
        queries.updateFilters(newFilters)
    }

    fun resetFilters() {
        queries.resetFilters()
    }
}