package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import data.local.entities.TimingMethod

class TimingMethodAdapter {
    companion object {
        val timingMethodValues = TimingMethod.values()
        val timingMethodOptions: JsonReader.Options =
            JsonReader.Options.of(*timingMethodValues.map { it.apiString }.toTypedArray())
    }

    @FromJson
    fun fromJson(reader: JsonReader): TimingMethod {
        return timingMethodValues[reader.selectString(timingMethodOptions)]
    }

    @ToJson
    fun toJson(value: TimingMethod): String {
        throw UnsupportedOperationException()
    }
}