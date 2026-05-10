package sms.pollub.gamerental.rental.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.rental.queries.RentalDto
import sms.pollub.gamerental.rental.queries.computeEffectiveStatus
import sms.pollub.gamerental.rental.repository.RentalRepository
import java.util.UUID

@Component
class GetRentalQueryHandler(
    private val rentalRepository: RentalRepository
) {
    @Transactional(readOnly = true)
    fun handle(rentalId: UUID): RentalDto {
        val rental = rentalRepository.findById(rentalId)
            .orElseThrow { NoSuchElementException("Rental with id '$rentalId' not found") }

        return RentalDto(
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
}

