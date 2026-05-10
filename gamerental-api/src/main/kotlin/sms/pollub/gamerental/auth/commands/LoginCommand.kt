package sms.pollub.gamerental.auth.commands

import jakarta.validation.constraints.NotBlank

data class LoginCommand(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

