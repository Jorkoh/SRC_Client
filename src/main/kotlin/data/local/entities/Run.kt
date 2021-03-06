package data.local.entities

import data.local.*
import ui.screens.home.Displayable
import java.util.*
import kotlin.time.Duration

enum class RunStatus(val apiString: String?, override val uiString: String) : Displayable {
    Pending("new", "Pending"),
    Approved("verified", "Approved"),
    PendingPlusApproved("no-api-string", "Pending + Approved"),
    Rejected("rejected", "Rejected");
}

data class Run(
    val runId: RunId,

    val gameId: GameId,
    val categoryId: CategoryId,
    val levelId: LevelId?,
    val variablesAndValuesIds: List<VariableAndValueIds>,

    val isEmulated: Boolean,
    val platformId: PlatformId?,
    val regionId: RegionId?,

    val runStatus: RunStatus,
    val verifierId: UserId?,

    val players: List<User>,

    val runDate: Date?,
    val submissionDate: Date?,
    val verificationDate: Date?,

    val primaryTime: Duration,
    val realTime: Duration,
    val realTimeNoLoads: Duration,
    val inGameTime: Duration,

    val comment: String?,
    val rejectionReason: String?,
    val videoText: String?,
    val videoLinks: List<String>,
    val weblink: String
)

data class FullRun(
    val runId: RunId,

    val gameId: GameId,
    val level: Level?,
    val category: Category,
    val variablesAndValuesIds: List<VariableAndValueIds>,

    val isEmulated: Boolean,
    val platformId: PlatformId?,
    val regionId: RegionId?,

    val runStatus: RunStatus,
    val verifierId: UserId?,
    val verifier: RegisteredUser?,

    val players: List<User>,

    val runDate: Date?,
    val submissionDate: Date?,
    val verificationDate: Date?,

    val primaryTime: Duration,
    val realTime: Duration,
    val realTimeNoLoads: Duration,
    val inGameTime: Duration,

    val comment: String?,
    val rejectionReason: String?,
    val videoText: String?,
    val videoLinks: List<String>,
    val weblink: String
)
