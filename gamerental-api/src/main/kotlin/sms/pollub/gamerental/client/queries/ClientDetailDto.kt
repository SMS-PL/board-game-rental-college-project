package sms.pollub.gamerental.client.queries

import sms.pollub.gamerental.rental.queries.RentalDto
import java.time.LocalDateTime
import java.util.UUID

data class ClientDetailDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val rentals: List<RentalDto>,
    val createdAt: LocalDateTime
)

