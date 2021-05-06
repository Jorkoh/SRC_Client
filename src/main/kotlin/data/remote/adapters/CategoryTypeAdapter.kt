package data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Options
import com.squareup.moshi.ToJson
import data.local.entities.CategoryType

class CategoryTypeAdapter {
    companion object {
        val categoryTypeValues = CategoryType.values()
        val categoryTypeOptions: Options = Options.of(*categoryTypeValues.map { it.apiString }.toTypedArray())
    }

    @FromJson
    fun fromJson(reader: JsonReader): CategoryType {
        return categoryTypeValues[reader.selectString(categoryTypeOptions)]
    }

    @ToJson
    fun toJson(value: CategoryType): String {
        throw UnsupportedOperationException()
    }
}