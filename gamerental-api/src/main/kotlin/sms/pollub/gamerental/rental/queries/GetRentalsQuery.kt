package sms.pollub.gamerental.rental.queries

import java.util.UUID

data class GetRentalsQuery(
    val status: String? = null,
    val clientId: UUID? = null,
    val gameId: UUID? = null
)

