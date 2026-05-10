package sms.pollub.gamerental.game.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.Game
import sms.pollub.gamerental.game.GameCopy
import sms.pollub.gamerental.game.commands.CreateGameCommand
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.game.repository.GameRepository
import java.util.UUID

@Component
class CreateGameCommandHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional
    fun handle(command: CreateGameCommand): UUID {
        val game = Game(
            title = command.title,
            description = command.description,
            tags = command.tags.toMutableSet(),
            totalCopies = command.totalCopies
        )
        gameRepository.save(game)

        repeat(command.totalCopies) { index ->
            gameCopyRepository.save(GameCopy(game = game, copyNumber = index + 1))
        }

        return game.id
    }
}

