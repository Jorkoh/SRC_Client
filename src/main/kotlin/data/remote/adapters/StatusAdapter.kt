package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Options
import com.squareup.moshi.ToJson
import data.local.entities.RunStatus

class StatusAdapter {
    companion object {
        val statusValues = RunStatus.values()
        val statusOptions: Options = Options.of(*statusValues.map { it.apiString }.toTypedArray())
    }

    @FromJson
    fun fromJson(reader: JsonReader): RunStatus {
        return statusValues[reader.selectString(statusOptions)]
    }

    @ToJson
    fun toJson(value: RunStatus): String {
        throw UnsupportedOperationException()
    }
}