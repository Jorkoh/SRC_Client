package data.local.entities

import data.local.RunId

enum class Status(val apiString : String) {
    Pending("new"),
    Approved("verified"),
    Rejected("rejected")
}

data class Run(
    val runId: RunId
)