package sms.pollub.gamerental.game.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.game.repository.GameRepository
import java.util.UUID

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

