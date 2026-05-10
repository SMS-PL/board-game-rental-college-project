package sms.pollub.gamerental.game.commands

import java.util.UUID

data class UpdateCopiesCommand(
    val gameId: UUID,
    val totalCopies: Int
)

