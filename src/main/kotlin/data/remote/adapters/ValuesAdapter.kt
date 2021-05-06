package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import data.remote.responses.ValueResponse
import data.remote.responses.ValueResponses
import data.remote.utils.nextBooleanOrNull
import data.remote.utils.nextStringOrNull
import data.remote.utils.readObject
import data.remote.utils.readObjectToList

class ValuesAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): ValueResponses {
        var values: List<ValueResponse> = emptyList()
        var defaultValueId: String? = null
        reader.readObject {
            do {
                when (reader.nextName()) {
                    "values" -> values = reader.readObjectToList {
                        readValue(reader)
                    }
                    "default" -> defaultValueId = reader.nextStringOrNull()
                    else -> reader.skipValue()
                }
            } while (reader.peek() == JsonReader.Token.NAME)
        }
        return ValueResponses(values, defaultValueId)
    }

    private fun readValue(reader: JsonReader): ValueResponse {
        val valueId = reader.nextName()
        var label: String? = null
        var rules: String? = null
        var miscellaneousFlag: Boolean? = null

        reader.readObject {
            do {
                when (reader.nextName()) {
                    "label" -> label = reader.nextString()
                    "rules" -> rules = reader.nextStringOrNull()
                    "flags" -> reader.readObject {
                        reader.skipName()
                        miscellaneousFlag = reader.nextBooleanOrNull()
                    }
                    else -> reader.skipValue()
                }
            } while (reader.peek() == JsonReader.Token.NAME)
        }

        return ValueResponse(
            id = valueId,
            label = label!!,
            rules = rules,
            miscellaneousFlag = miscellaneousFlag
        )
    }

    @ToJson
    fun toJson(value: ValueResponses): String {
        throw UnsupportedOperationException()
    }
}