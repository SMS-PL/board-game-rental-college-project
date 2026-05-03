package sms.pollub.gamerental.client

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sms.pollub.gamerental.client.commands.CreateClientCommand
import sms.pollub.gamerental.client.commands.CreateClientCommandHandler
import sms.pollub.gamerental.client.commands.DeleteClientCommandHandler
import sms.pollub.gamerental.client.commands.UpdateClientCommand
import sms.pollub.gamerental.client.commands.UpdateClientCommandHandler
import sms.pollub.gamerental.client.commands.UpdateClientRequest
import sms.pollub.gamerental.client.queries.ClientDetailDto
import sms.pollub.gamerental.client.queries.ClientDto
import sms.pollub.gamerental.client.queries.GetClientQueryHandler
import sms.pollub.gamerental.client.queries.GetClientsQuery
import sms.pollub.gamerental.client.queries.GetClientsQueryHandler
import java.util.UUID

@RestController
@RequestMapping("/api/clients")
class ClientController(
    private val createClientCommandHandler: CreateClientCommandHandler,
    private val updateClientCommandHandler: UpdateClientCommandHandler,
    private val deleteClientCommandHandler: DeleteClientCommandHandler,
    private val getClientsQueryHandler: GetClientsQueryHandler,
    private val getClientQueryHandler: GetClientQueryHandler
) {

    @GetMapping
    fun getClients(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "lastName") sortBy: String
    ): List<ClientDto> = getClientsQueryHandler.handle(GetClientsQuery(search, sortBy))

    @GetMapping("/{id}")
    fun getClient(@PathVariable id: UUID): ClientDetailDto = getClientQueryHandler.handle(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createClient(@Valid @RequestBody command: CreateClientCommand): Map<String, UUID> {
        val id = createClientCommandHandler.handle(command)
        return mapOf("id" to id)
    }

    @PutMapping("/{id}")
    fun updateClient(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateClientRequest
    ): ClientDetailDto {
        updateClientCommandHandler.handle(
            UpdateClientCommand(
                id = id,
                firstName = request.firstName,
                lastName = request.lastName,
                phone = request.phone,
                email = request.email
            )
        )
        return getClientQueryHandler.handle(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteClient(@PathVariable id: UUID) = deleteClientCommandHandler.handle(id)
}

