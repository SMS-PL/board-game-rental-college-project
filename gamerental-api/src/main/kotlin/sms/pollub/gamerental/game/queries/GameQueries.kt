package sms.pollub.gamerental.game.queries

import sms.pollub.gamerental.game.CopyCondition
import sms.pollub.gamerental.game.GameTag
import java.time.LocalDateTime
import java.util.UUID

data class GetGamesQuery(
    val tag: GameTag? = null,
    val sortBy: String = "title"
)

data class GameDto(
    val id: UUID,
    val title: String,
    val description: String,
    val tags: Set<GameTag>,
    val totalCopies: Int,
    val availableCopies: Int,
    val createdAt: LocalDateTime
)

data class GameCopyDto(
    val id: UUID,
    val copyNumber: Int,
    val condition: CopyCondition,
    val isAvailable: Boolean
)

data class GameDetailDto(
    val id: UUID,
    val title: String,
    val description: String,
    val tags: Set<GameTag>,
    val totalCopies: Int,
    val copies: List<GameCopyDto>,
    val createdAt: LocalDateTime
)

