package sms.pollub.gamerental.rental.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.repository.ClientRepository
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.rental.Rental
import sms.pollub.gamerental.rental.commands.IssueGameCommand
import sms.pollub.gamerental.rental.repository.RentalRepository
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

