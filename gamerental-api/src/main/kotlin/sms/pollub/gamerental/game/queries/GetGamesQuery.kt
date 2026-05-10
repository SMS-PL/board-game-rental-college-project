package sms.pollub.gamerental.game.queries

import sms.pollub.gamerental.game.enums.GameTag
import java.time.LocalDateTime
import java.util.UUID

data class GetGamesQuery(
    val tag: GameTag? = null,
    val sortBy: String = "title"
)

