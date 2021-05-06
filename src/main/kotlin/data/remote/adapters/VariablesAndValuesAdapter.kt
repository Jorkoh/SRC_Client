package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import data.remote.responses.VariableAndValueResponse
import data.remote.responses.VariablesAndValuesResponses
import data.remote.utils.readObjectToList

class VariablesAndValuesAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): VariablesAndValuesResponses {
        return VariablesAndValuesResponses(reader.readObjectToList {
            VariableAndValueResponse(reader.nextName(), reader.nextString())
        })
    }

    @ToJson
    fun toJson(value: VariablesAndValuesResponses): String {
        throw UnsupportedOperationException()
    }
}