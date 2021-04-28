package data

import data.local.GameId
import data.local.RunId
import data.local.entities.Run
import data.remote.responses.GameResponse
import data.remote.responses.RunResponse
import settings.database.Game

fun GameResponse.toGame() = Game(GameId(id), abbreviation, names.international)

fun RunResponse.toRun() = Run(RunId(id))