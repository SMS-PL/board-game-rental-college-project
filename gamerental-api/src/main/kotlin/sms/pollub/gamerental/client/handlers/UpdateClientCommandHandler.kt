package sms.pollub.gamerental.client.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.commands.UpdateClientCommand
import sms.pollub.gamerental.client.repository.ClientRepository

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

