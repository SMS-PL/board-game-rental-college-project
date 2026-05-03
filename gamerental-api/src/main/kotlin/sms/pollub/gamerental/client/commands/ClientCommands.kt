package sms.pollub.gamerental.client.commands

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateClientCommand(
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    val phone: String? = null,
    val email: String? = null
)

data class UpdateClientRequest(
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    val phone: String? = null,
    val email: String? = null
)

data class UpdateClientCommand(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?
)

