package sms.pollub.gamerental.client.commands

import jakarta.validation.constraints.NotBlank

data class UpdateClientRequest(
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    val phone: String? = null,
    val email: String? = null
)

