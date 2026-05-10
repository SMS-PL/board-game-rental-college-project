package sms.pollub.gamerental.game.commands

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import sms.pollub.gamerental.game.enums.GameTag

data class CreateGameCommand(
    @field:NotBlank val title: String,
    @field:NotBlank val description: String,
    @field:NotNull val tags: Set<GameTag>,
    @field:Min(0) val totalCopies: Int = 0
)

