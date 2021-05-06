package data.local.entities

import data.local.CategoryId
import data.local.ValueId
import data.local.VariableId

enum class VariableScope(val apiString: String){
    Global("global"),
    FullGame("full-game"),
    AllLevels("all-levels"),
    SingleLevel("single-level")
}

data class Variable (
    val variableId : VariableId,
    val name : String,

    val categoryId: CategoryId?,
    val scope: VariableScope,

    val isMandatory : Boolean,
    val isUserDefined : Boolean,
    val isSubCategory : Boolean,
    val obsoletes : Boolean,

    val values : List<Value>,
    val defaultValueId : ValueId?
)

data class Value(
    val valueId: ValueId,
    val label : String,
    val rules : String?,
    val miscellaneousFlag : Boolean?
)

data class VariableAndValue(
    val variableId: VariableId,
    val valueId: ValueId
)