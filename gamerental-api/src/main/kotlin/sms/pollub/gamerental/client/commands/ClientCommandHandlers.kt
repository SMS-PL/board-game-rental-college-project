package sms.pollub.gamerental.client.commands

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.Client
import sms.pollub.gamerental.client.ClientRepository
import sms.pollub.gamerental.rental.RentalRepository
import java.util.UUID

@Component
class CreateClientCommandHandler(
    private val clientRepository: ClientRepository
) {
    @Transactional
    fun handle(command: CreateClientCommand): UUID {
        val client = Client(
            firstName = command.firstName,
            lastName = command.lastName,
            phone = command.phone,
            email = command.email
        )
        return clientRepository.save(client).id
    }
}

@Component
class UpdateClientCommandHandler(
    private val clientRepository: ClientRepository
) {
    @Transactional
    fun handle(command: UpdateClientCommand) {
        val client = clientRepository.findById(command.id)
            .orElseThrow { NoSuchElementException("Client with id '${command.id}' not found") }

        client.firstName = command.firstName
        client.lastName = command.lastName
        client.phone = command.phone
        client.email = command.email
    }
}

@Component
class DeleteClientCommandHandler(
    private val clientRepository: ClientRepository,
    private val rentalRepository: RentalRepository
) {
    @Transactional
    fun handle(clientId: UUID) {
        val activeRentals = rentalRepository.countByClientIdAndStatus(clientId, sms.pollub.gamerental.rental.RentalStatus.ACTIVE)
        if (activeRentals > 0) {
            throw IllegalStateException("Cannot delete client with active rentals")
        }
        if (!clientRepository.existsById(clientId)) {
            throw NoSuchElementException("Client with id '$clientId' not found")
        }
        clientRepository.deleteById(clientId)
    }
}

