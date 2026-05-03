package sms.pollub.gamerental.rental.queries

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.CopyCondition
import sms.pollub.gamerental.rental.RentalRepository
import sms.pollub.gamerental.rental.RentalStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class GetRentalsQuery(
    val status: String? = null,
    val clientId: UUID? = null,
    val gameId: UUID? = null
)

data class RentalDto(
    val id: UUID,
    val gameCopyId: UUID,
    val gameTitle: String,
    val copyNumber: Int,
    val clientId: UUID,
    val clientName: String,
    val rentedFrom: LocalDate,
    val dueTo: LocalDate,
    val returnedAt: LocalDate?,
    val conditionOnReturn: CopyCondition?,
    val status: RentalStatus,
    val notes: String?,
    val createdAt: LocalDateTime
)

fun computeEffectiveStatus(status: RentalStatus, dueTo: LocalDate): RentalStatus =
    if (status == RentalStatus.ACTIVE && dueTo.isBefore(LocalDate.now())) RentalStatus.OVERDUE else status

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

