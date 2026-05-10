package sms.pollub.gamerental.client.queries

import java.time.LocalDateTime
import java.util.UUID

data class ClientDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val activeRentalsCount: Int,
    val createdAt: LocalDateTime
)

