package data.local.entities

import data.local.*
import java.util.*

// TODO move this somewhere else
enum class Status(val apiString: String) {
    Pending("new"),
    Approved("verified"),
    Rejected("rejected")
}

// TODO move this somewhere else
data class Variable(
    val id: String,
    val value: String
)

// TODO add system from response?
data class Run(
    val runId: RunId,

    val gameId: GameId,
    val categoryId: CategoryId,
    val levelId: LevelId?,
    val variables: List<Variable>,

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

