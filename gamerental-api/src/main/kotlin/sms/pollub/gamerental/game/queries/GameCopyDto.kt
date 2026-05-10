package sms.pollub.gamerental.game.queries

import sms.pollub.gamerental.game.enums.CopyCondition
import java.util.UUID

data class GameCopyDto(
    val id: UUID,
    val copyNumber: Int,
    val condition: CopyCondition,
    val isAvailable: Boolean
)

