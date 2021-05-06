package data.remote.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import data.local.entities.VariableScope

@JsonClass(generateAdapter = true)
data class VariableResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "category")
    val categoryId: String?,
    @Json(name = "scope")
    val scope: Scope,
    @Json(name = "mandatory")
    val mandatory: Boolean,
    @Json(name = "user-defined")
    val userDefined: Boolean,
    @Json(name = "is-subcategory")
    val isSubCategory: Boolean,
    @Json(name = "obsoletes")
    val obsoletes: Boolean,

    @Json(name = "values")
    val valueResponses: ValueResponses,

    @Json(name = "links")
    val links: List<Link>,
)

@JsonClass(generateAdapter = true)
data class Scope(
    @Json(name = "type")
    val value: VariableScope,
    @Json(name = "level")
    val levelId: String?
)

data class ValueResponses(
    val valueResponses: List<ValueResponse>,
    val defaultValueId: String?
)

data class ValueResponse(
    val id : String,
    val label : String,
    val rules : String?,
    val miscellaneousFlag : Boolean?
)