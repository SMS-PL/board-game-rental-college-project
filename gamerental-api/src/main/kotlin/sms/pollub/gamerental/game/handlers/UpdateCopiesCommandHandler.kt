package sms.pollub.gamerental.game.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.GameCopy
import sms.pollub.gamerental.game.commands.UpdateCopiesCommand
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.game.repository.GameRepository

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

