package sms.pollub.gamerental.client.queries

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.ClientRepository
import sms.pollub.gamerental.game.CopyCondition
import sms.pollub.gamerental.game.GameTag
import sms.pollub.gamerental.rental.RentalRepository
import sms.pollub.gamerental.rental.RentalStatus
import sms.pollub.gamerental.rental.queries.RentalDto
import sms.pollub.gamerental.rental.queries.computeEffectiveStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class GetClientsQuery(
    val search: String? = null,
    val sortBy: String = "lastName"
)

data class ClientDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val activeRentalsCount: Int,
    val createdAt: LocalDateTime
)

data class ClientDetailDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val rentals: List<RentalDto>,
    val createdAt: LocalDateTime
)

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

@Component
class GetClientQueryHandler(
    private val clientRepository: ClientRepository,
    private val rentalRepository: RentalRepository
) {
    @Transactional(readOnly = true)
    fun handle(clientId: UUID): ClientDetailDto {
        val client = clientRepository.findById(clientId)
            .orElseThrow { NoSuchElementException("Client with id '$clientId' not found") }

        val rentals = rentalRepository.findByClientId(clientId)
            .sortedByDescending { it.createdAt }
            .map { rental ->
                RentalDto(
                    id = rental.id,
                    gameCopyId = rental.gameCopy.id,
                    gameTitle = rental.gameCopy.game.title,
                    copyNumber = rental.gameCopy.copyNumber,
                    clientId = rental.client.id,
                    clientName = "${rental.client.firstName} ${rental.client.lastName}",
                    rentedFrom = rental.rentedFrom,
                    dueTo = rental.dueTo,
                    returnedAt = rental.returnedAt,
                    conditionOnReturn = rental.conditionOnReturn,
                    status = computeEffectiveStatus(rental.status, rental.dueTo),
                    notes = rental.notes,
                    createdAt = rental.createdAt
                )
            }

        return ClientDetailDto(
            id = client.id,
            firstName = client.firstName,
            lastName = client.lastName,
            phone = client.phone,
            email = client.email,
            rentals = rentals,
            createdAt = client.createdAt
        )
    }
}

