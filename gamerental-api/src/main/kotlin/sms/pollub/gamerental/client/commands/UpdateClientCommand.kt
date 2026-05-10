package sms.pollub.gamerental.client.commands

import java.util.UUID

data class UpdateClientCommand(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?
)

