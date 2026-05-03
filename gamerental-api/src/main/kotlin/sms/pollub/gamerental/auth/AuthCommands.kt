package sms.pollub.gamerental.auth

import jakarta.validation.constraints.NotBlank

data class RegisterCommand(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

data class LoginCommand(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

data class TokenResponse(val token: String)

