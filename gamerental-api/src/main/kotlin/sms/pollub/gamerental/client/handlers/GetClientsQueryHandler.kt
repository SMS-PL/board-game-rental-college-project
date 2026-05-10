package sms.pollub.gamerental.client.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.queries.ClientDto
import sms.pollub.gamerental.client.queries.GetClientsQuery
import sms.pollub.gamerental.client.repository.ClientRepository
import sms.pollub.gamerental.rental.enums.RentalStatus
import sms.pollub.gamerental.rental.repository.RentalRepository

@Component
class GetClientsQueryHandler(
    private val clientRepository: ClientRepository,
    private val rentalRepository: RentalRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: GetClientsQuery): List<ClientDto> {
        val clients = if (!query.search.isNullOrBlank()) {
            clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query.search, query.search
            )
        } else {
            clientRepository.findAll()
        }

        val sorted = when (query.sortBy.lowercase()) {
            "firstname" -> clients.sortedBy { it.firstName.lowercase() }
            "createdat", "date" -> clients.sortedBy { it.createdAt }
            else -> clients.sortedBy { it.lastName.lowercase() }
        }

        return sorted.map { client ->
            val activeRentals = rentalRepository.countByClientIdAndStatus(client.id, RentalStatus.ACTIVE)
            ClientDto(
                id = client.id,
                firstName = client.firstName,
                lastName = client.lastName,
                phone = client.phone,
                email = client.email,
                activeRentalsCount = activeRentals.toInt(),
                createdAt = client.createdAt
            )
        }
    }
}

