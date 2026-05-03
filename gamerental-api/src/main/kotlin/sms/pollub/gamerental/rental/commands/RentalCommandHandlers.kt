package sms.pollub.gamerental.rental.commands

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.ClientRepository
import sms.pollub.gamerental.game.GameCopyRepository
import sms.pollub.gamerental.rental.Rental
import sms.pollub.gamerental.rental.RentalRepository
import sms.pollub.gamerental.rental.RentalStatus
import java.util.UUID

@Component
class IssueGameCommandHandler(
    private val rentalRepository: RentalRepository,
    private val gameCopyRepository: GameCopyRepository,
    private val clientRepository: ClientRepository
) {
    @Transactional
    fun handle(command: IssueGameCommand): UUID {
        if (command.dueTo.isBefore(command.rentedFrom)) {
            throw IllegalArgumentException("Due date cannot be before rental date")
        }

        val gameCopy = gameCopyRepository.findById(command.gameCopyId)
            .orElseThrow { NoSuchElementException("Game copy with id '${command.gameCopyId}' not found") }

        if (!gameCopy.isAvailable) {
            throw IllegalStateException("Game copy '${command.gameCopyId}' is not available for rental")
        }

        val client = clientRepository.findById(command.clientId)
            .orElseThrow { NoSuchElementException("Client with id '${command.clientId}' not found") }

        gameCopy.isAvailable = false
        gameCopyRepository.save(gameCopy)

        val rental = Rental(
            gameCopy = gameCopy,
            client = client,
            rentedFrom = command.rentedFrom,
            dueTo = command.dueTo,
            notes = command.notes
        )
        return rentalRepository.save(rental).id
    }
}

@Component
class ReturnGameCommandHandler(
    private val rentalRepository: RentalRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional
    fun handle(rentalId: UUID, command: ReturnGameCommand) {
        val rental = rentalRepository.findById(rentalId)
            .orElseThrow { NoSuchElementException("Rental with id '$rentalId' not found") }

        if (rental.status == RentalStatus.RETURNED) {
            throw IllegalStateException("Rental '$rentalId' has already been returned")
        }

        rental.returnedAt = command.returnedAt
        rental.conditionOnReturn = command.conditionOnReturn
        rental.status = RentalStatus.RETURNED

        val gameCopy = rental.gameCopy
        gameCopy.isAvailable = true
        gameCopy.condition = command.conditionOnReturn
        gameCopyRepository.save(gameCopy)

        rentalRepository.save(rental)
    }
}

