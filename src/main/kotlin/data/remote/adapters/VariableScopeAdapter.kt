package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Options
import com.squareup.moshi.ToJson
import data.local.entities.VariableScope

class VariableScopeAdapter {
    companion object {
        val variableScopeValues = VariableScope.values()
        val variableScopeOptions: Options = Options.of(*variableScopeValues.map { it.apiString }.toTypedArray())
    }

    @FromJson
    fun fromJson(reader: JsonReader): VariableScope {
        return variableScopeValues[reader.selectString(variableScopeOptions)]
    }

    @ToJson
    fun toJson(value: VariableScope): String {
        throw UnsupportedOperationException()
    }
}