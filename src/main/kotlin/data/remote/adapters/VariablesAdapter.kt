package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import data.remote.responses.Variable
import data.remote.responses.Variables
import data.remote.utils.readObjectToList

class VariablesAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Variables {
        return Variables(reader.readObjectToList {
            Variable(reader.nextName(), reader.nextString())
        })
    }

    @ToJson
    fun toJson(value: Variables): String {
        throw UnsupportedOperationException()
    }
}