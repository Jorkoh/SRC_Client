package data.local.entities

import data.local.*
import java.util.*

enum class Status(val apiString: String) {
    Pending("new"),
    Approved("verified"),
    Rejected("rejected")
}

data class Run(
    val runId: RunId,

    val gameId: GameId,
    val categoryId: CategoryId,
    val levelId: LevelId?,
    val variablesAndValues: List<VariableAndValue>,

    val status: Status,
    val verifierId: UserId?,
    val verificationDate: Date?,

    val players: List<User>,

    val comment: String?,
    val runDate: Date,
    val submissionDate: Date?,
    val timePrimary: Double,
    val timeReal: Double?,
    val timeRealNoLoads: Double?,
    val timeIngame: Double?,
    val videoText: String?,
    val videoLinks: List<String>,

    val weblink: String
)

