package sms.pollub.gamerental.rental.commands

import jakarta.validation.constraints.NotNull
import sms.pollub.gamerental.game.enums.CopyCondition
import java.time.LocalDate

data class ReturnGameCommand(
    @field:NotNull val conditionOnReturn: CopyCondition,
    val returnedAt: LocalDate = LocalDate.now()
)

