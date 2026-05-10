package sms.pollub.gamerental.rental.commands

import jakarta.validation.constraints.NotNull
import sms.pollub.gamerental.game.enums.CopyCondition
import java.time.LocalDate
import java.util.UUID

data class IssueGameCommand(
    @field:NotNull val gameCopyId: UUID,
    @field:NotNull val clientId: UUID,
    @field:NotNull val rentedFrom: LocalDate,
    @field:NotNull val dueTo: LocalDate,
    val notes: String? = null
)

