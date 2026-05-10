package sms.pollub.gamerental.client.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.repository.ClientRepository
import sms.pollub.gamerental.rental.enums.RentalStatus
import sms.pollub.gamerental.rental.repository.RentalRepository
import java.util.UUID

@Component
class DeleteClientCommandHandler(
    private val clientRepository: ClientRepository,
    private val rentalRepository: RentalRepository
) {
    @Transactional
    fun handle(clientId: UUID) {
        val activeRentals = rentalRepository.countByClientIdAndStatus(clientId, RentalStatus.ACTIVE)
        if (activeRentals > 0) {
            throw IllegalStateException("Cannot delete client with active rentals")
        }
        if (!clientRepository.existsById(clientId)) {
            throw NoSuchElementException("Client with id '$clientId' not found")
        }
        clientRepository.deleteById(clientId)
    }
}

