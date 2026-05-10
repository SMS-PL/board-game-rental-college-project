package sms.pollub.gamerental.game.commands

import sms.pollub.gamerental.game.enums.GameTag
import java.util.UUID

data class UpdateGameCommand(
    val id: UUID,
    val title: String,
    val description: String,
    val tags: Set<GameTag>
)

