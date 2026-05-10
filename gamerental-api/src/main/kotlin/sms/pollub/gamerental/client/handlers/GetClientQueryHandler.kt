package sms.pollub.gamerental.client.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.queries.ClientDetailDto
import sms.pollub.gamerental.client.repository.ClientRepository
import sms.pollub.gamerental.rental.queries.RentalDto
import sms.pollub.gamerental.rental.queries.computeEffectiveStatus
import sms.pollub.gamerental.rental.repository.RentalRepository
import java.util.UUID

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

