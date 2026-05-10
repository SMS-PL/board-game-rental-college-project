package sms.pollub.gamerental.rental.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.rental.enums.RentalStatus
import sms.pollub.gamerental.rental.queries.GetRentalsQuery
import sms.pollub.gamerental.rental.queries.RentalDto
import sms.pollub.gamerental.rental.queries.computeEffectiveStatus
import sms.pollub.gamerental.rental.repository.RentalRepository
import java.time.LocalDate

@Component
class GetRentalsQueryHandler(
    private val rentalRepository: RentalRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: GetRentalsQuery): List<RentalDto> {
        val statusFilter = query.status?.uppercase()

        val rentals = when (statusFilter) {
            "OVERDUE" -> rentalRepository.findOverdue(LocalDate.now())
            "ACTIVE" -> rentalRepository.findByFilters(query.clientId, query.gameId)
                .filter { it.status == RentalStatus.ACTIVE && !it.dueTo.isBefore(LocalDate.now()) }
            "RETURNED" -> rentalRepository.findByFilters(query.clientId, query.gameId)
                .filter { it.status == RentalStatus.RETURNED }
            else -> rentalRepository.findByFilters(query.clientId, query.gameId)
        }

        val filteredByGame = if (query.gameId != null && statusFilter == "OVERDUE") {
            rentals.filter { it.gameCopy.game.id == query.gameId }
        } else rentals

        val filteredByClient = if (query.clientId != null && statusFilter == "OVERDUE") {
            filteredByGame.filter { it.client.id == query.clientId }
        } else filteredByGame

        return filteredByClient
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
    }
}

