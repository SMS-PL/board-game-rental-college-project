package sms.pollub.gamerental.game

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sms.pollub.gamerental.game.commands.CreateGameCommand
import sms.pollub.gamerental.game.commands.CreateGameCommandHandler
import sms.pollub.gamerental.game.commands.DeleteGameCommandHandler
import sms.pollub.gamerental.game.commands.UpdateCopiesCommand
import sms.pollub.gamerental.game.commands.UpdateCopiesCommandHandler
import sms.pollub.gamerental.game.commands.UpdateCopiesRequest
import sms.pollub.gamerental.game.commands.UpdateGameCommand
import sms.pollub.gamerental.game.commands.UpdateGameCommandHandler
import sms.pollub.gamerental.game.commands.UpdateGameRequest
import sms.pollub.gamerental.game.queries.GameCopyDto
import sms.pollub.gamerental.game.queries.GameDetailDto
import sms.pollub.gamerental.game.queries.GameDto
import sms.pollub.gamerental.game.queries.GetAvailableCopiesQueryHandler
import sms.pollub.gamerental.game.queries.GetGameQueryHandler
import sms.pollub.gamerental.game.queries.GetGamesQuery
import sms.pollub.gamerental.game.queries.GetGamesQueryHandler
import java.util.UUID

@RestController
@RequestMapping("/api/games")
class GameController(
    private val createGameCommandHandler: CreateGameCommandHandler,
    private val updateGameCommandHandler: UpdateGameCommandHandler,
    private val deleteGameCommandHandler: DeleteGameCommandHandler,
    private val updateCopiesCommandHandler: UpdateCopiesCommandHandler,
    private val getGamesQueryHandler: GetGamesQueryHandler,
    private val getGameQueryHandler: GetGameQueryHandler,
    private val getAvailableCopiesQueryHandler: GetAvailableCopiesQueryHandler
) {

    @GetMapping
    fun getGames(
        @RequestParam(required = false) tag: GameTag?,
        @RequestParam(defaultValue = "title") sortBy: String
    ): List<GameDto> = getGamesQueryHandler.handle(GetGamesQuery(tag, sortBy))

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: UUID): GameDetailDto = getGameQueryHandler.handle(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createGame(@Valid @RequestBody command: CreateGameCommand): Map<String, UUID> {
        val id = createGameCommandHandler.handle(command)
        return mapOf("id" to id)
    }

    @PutMapping("/{id}")
    fun updateGame(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateGameRequest
    ): GameDetailDto {
        updateGameCommandHandler.handle(
            UpdateGameCommand(id = id, title = request.title, description = request.description, tags = request.tags)
        )
        return getGameQueryHandler.handle(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteGame(@PathVariable id: UUID) = deleteGameCommandHandler.handle(id)

    @PatchMapping("/{id}/copies")
    fun updateCopies(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateCopiesRequest
    ): GameDetailDto {
        updateCopiesCommandHandler.handle(UpdateCopiesCommand(gameId = id, totalCopies = request.totalCopies))
        return getGameQueryHandler.handle(id)
    }

    @GetMapping("/{id}/available-copies")
    fun getAvailableCopies(@PathVariable id: UUID): List<GameCopyDto> =
        getAvailableCopiesQueryHandler.handle(id)
}
