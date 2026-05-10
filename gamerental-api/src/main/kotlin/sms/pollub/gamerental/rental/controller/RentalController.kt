package sms.pollub.gamerental.rental.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sms.pollub.gamerental.rental.commands.IssueGameCommand
import sms.pollub.gamerental.rental.commands.ReturnGameCommand
import sms.pollub.gamerental.rental.handlers.GetRentalQueryHandler
import sms.pollub.gamerental.rental.handlers.GetRentalsQueryHandler
import sms.pollub.gamerental.rental.handlers.IssueGameCommandHandler
import sms.pollub.gamerental.rental.handlers.ReturnGameCommandHandler
import sms.pollub.gamerental.rental.queries.GetRentalsQuery
import sms.pollub.gamerental.rental.queries.RentalDto
import java.util.UUID

@RestController
@RequestMapping("/api/rentals")
class RentalController(
    private val issueGameCommandHandler: IssueGameCommandHandler,
    private val returnGameCommandHandler: ReturnGameCommandHandler,
    private val getRentalsQueryHandler: GetRentalsQueryHandler,
    private val getRentalQueryHandler: GetRentalQueryHandler
) {

    @GetMapping
    fun getRentals(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) clientId: UUID?,
        @RequestParam(required = false) gameId: UUID?
    ): List<RentalDto> = getRentalsQueryHandler.handle(GetRentalsQuery(status, clientId, gameId))

    @GetMapping("/{id}")
    fun getRental(@PathVariable id: UUID): RentalDto = getRentalQueryHandler.handle(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun issueGame(@Valid @RequestBody command: IssueGameCommand): Map<String, UUID> {
        val id = issueGameCommandHandler.handle(command)
        return mapOf("id" to id)
    }

    @PatchMapping("/{id}/return")
    fun returnGame(
        @PathVariable id: UUID,
        @Valid @RequestBody command: ReturnGameCommand
    ): RentalDto {
        returnGameCommandHandler.handle(id, command)
        return getRentalQueryHandler.handle(id)
    }
}

