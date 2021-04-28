package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Options
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import data.local.entities.Status
import data.remote.responses.Variable
import data.remote.utils.readArrayToList

class StatusAdapter {
    companion object {
        val statusValues = Status.values()
        val statusOptions: Options = Options.of(*statusValues.map { it.apiString }.toTypedArray())
    }

    @FromJson
    fun fromJson(reader: JsonReader): Status {
        return statusValues[reader.selectString(statusOptions)]
    }

    @ToJson
    fun toJson(value: Status): String {
        throw UnsupportedOperationException()
    }
}