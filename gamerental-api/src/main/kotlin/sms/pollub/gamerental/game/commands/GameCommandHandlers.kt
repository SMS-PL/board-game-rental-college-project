package sms.pollub.gamerental.game.commands

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.Game
import sms.pollub.gamerental.game.GameCopy
import sms.pollub.gamerental.game.GameCopyRepository
import sms.pollub.gamerental.game.GameRepository
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

@Component
class DeleteGameCommandHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional
    fun handle(gameId: UUID) {
        val rentedCopies = gameCopyRepository.countByGameIdAndIsAvailableFalse(gameId)
        if (rentedCopies > 0) {
            throw IllegalStateException("Cannot delete game with active rentals")
        }
        if (!gameRepository.existsById(gameId)) {
            throw NoSuchElementException("Game with id '$gameId' not found")
        }
        gameRepository.deleteById(gameId)
    }
}

@Component
class UpdateCopiesCommandHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional
    fun handle(command: UpdateCopiesCommand) {
        val game = gameRepository.findById(command.gameId)
            .orElseThrow { NoSuchElementException("Game with id '${command.gameId}' not found") }

        val currentCopies = gameCopyRepository.findByGameId(game.id)
        val currentTotal = currentCopies.size
        val newTotal = command.totalCopies

        when {
            newTotal > currentTotal -> {
                val maxNumber = currentCopies.maxOfOrNull { it.copyNumber } ?: 0
                for (i in 1..(newTotal - currentTotal)) {
                    gameCopyRepository.save(GameCopy(game = game, copyNumber = maxNumber + i))
                }
            }
            newTotal < currentTotal -> {
                val rentedCount = gameCopyRepository.countByGameIdAndIsAvailableFalse(game.id)
                if (newTotal < rentedCount) {
                    throw IllegalStateException(
                        "Cannot reduce copies below rented count ($rentedCount currently rented)"
                    )
                }
                val toRemove = currentCopies
                    .filter { it.isAvailable }
                    .sortedByDescending { it.copyNumber }
                    .take(currentTotal - newTotal)
                gameCopyRepository.deleteAll(toRemove)
            }
        }

        game.totalCopies = newTotal
    }
}

