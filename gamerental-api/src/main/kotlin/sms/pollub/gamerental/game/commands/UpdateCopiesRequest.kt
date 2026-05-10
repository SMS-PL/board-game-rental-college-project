package sms.pollub.gamerental.game.commands

import jakarta.validation.constraints.Min

data class UpdateCopiesRequest(
    @field:Min(0) val totalCopies: Int
)

