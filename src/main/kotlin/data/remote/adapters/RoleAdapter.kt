package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Options
import com.squareup.moshi.ToJson
import data.local.entities.Role

class RoleAdapter {
    companion object {
        val roleValues = Role.values()
        val roleOptions: Options = Options.of(*roleValues.map { it.apiString }.toTypedArray())
    }

    @FromJson
    fun fromJson(reader: JsonReader): Role {
        return roleValues[reader.selectString(roleOptions)]
    }

    @ToJson
    fun toJson(value: Role): String {
        throw UnsupportedOperationException()
    }
}