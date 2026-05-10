package sms.pollub.gamerental.rental.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.rental.commands.ReturnGameCommand
import sms.pollub.gamerental.rental.enums.RentalStatus
import sms.pollub.gamerental.rental.repository.RentalRepository
import java.util.UUID

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

