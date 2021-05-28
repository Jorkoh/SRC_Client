package data.local.entities

import data.local.CategoryId
import data.local.LevelId
import data.local.ValueId
import data.local.VariableId
import kotlinx.serialization.Serializable
import ui.screens.home.Displayable

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
    val levelId: LevelId?,

    val isMandatory : Boolean,
    val isUserDefined : Boolean,
    val isSubCategory : Boolean,
    val obsoletes : Boolean,

    val values : List<Value>,
    val defaultValueId : ValueId?
) : Displayable {
    override val uiString: String
        get() = name
}

data class Value(
    val valueId: ValueId,
    val label : String,
    val rules : String?,
    val miscellaneousFlag : Boolean?
) : Displayable {
    override val uiString: String
        get() = label
}

@Serializable
data class VariableAndValueIds(
    val variableId: VariableId,
    val valueId: ValueId
)