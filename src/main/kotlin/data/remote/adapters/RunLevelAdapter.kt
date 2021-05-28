package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import data.remote.responses.LevelResponse
import data.remote.responses.RunLevel
import data.remote.utils.readObject

class RunLevelAdapter {
    companion object {
        val levelAdapter = Moshi.Builder().build().adapter(LevelResponse::class.java)
    }

    @FromJson
    fun fromJson(reader: JsonReader): RunLevel {
        var level: LevelResponse? = null
        reader.readObject {
            reader.skipName()
            // when level is null API returns empty array instead of null on this embed :/
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                level = levelAdapter.fromJson(reader)
            } else {
                reader.skipValue()
            }
        }
        return RunLevel(level)
    }

    @ToJson
    fun toJson(value: RunLevel): String {
        throw UnsupportedOperationException()
    }
}