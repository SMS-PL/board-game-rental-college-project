package sms.pollub.gamerental.game.queries

import sms.pollub.gamerental.game.enums.GameTag
import java.time.LocalDateTime
import java.util.UUID

data class GameDto(
    val id: UUID,
    val title: String,
    val description: String,
    val tags: Set<GameTag>,
    val totalCopies: Int,
    val availableCopies: Int,
    val createdAt: LocalDateTime
)

