package sms.pollub.gamerental.client.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.client.Client
import sms.pollub.gamerental.client.commands.CreateClientCommand
import sms.pollub.gamerental.client.repository.ClientRepository
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

