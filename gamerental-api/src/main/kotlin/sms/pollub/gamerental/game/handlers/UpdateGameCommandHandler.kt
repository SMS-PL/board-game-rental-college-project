package sms.pollub.gamerental.game.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.commands.UpdateGameCommand
import sms.pollub.gamerental.game.repository.GameRepository

@Component
class UpdateGameCommandHandler(
    private val gameRepository: GameRepository
) {
    @Transactional
    fun handle(command: UpdateGameCommand) {
        val game = gameRepository.findById(command.id)
            .orElseThrow { NoSuchElementException("Game with id '${command.id}' not found") }

        game.title = command.title
        game.description = command.description
        game.tags = command.tags.toMutableSet()
    }
}

