package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Options
import com.squareup.moshi.ToJson
import data.local.entities.PlayerCountType

class PlayerCountTypeAdapter {
    companion object {
        val playerCountTypeValues = PlayerCountType.values()
        val playerCountTypeOptions: Options = Options.of(*playerCountTypeValues.map { it.apiString }.toTypedArray())
    }

    @FromJson
    fun fromJson(reader: JsonReader): PlayerCountType {
        return playerCountTypeValues[reader.selectString(playerCountTypeOptions)]
    }

    @ToJson
    fun toJson(value: PlayerCountType): String {
        throw UnsupportedOperationException()
    }
}