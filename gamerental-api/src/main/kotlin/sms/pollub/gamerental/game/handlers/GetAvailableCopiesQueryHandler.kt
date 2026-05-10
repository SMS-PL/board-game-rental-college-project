package sms.pollub.gamerental.game.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.queries.GameCopyDto
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.game.repository.GameRepository
import java.util.UUID

@Component
class GetAvailableCopiesQueryHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(gameId: UUID): List<GameCopyDto> {
        if (!gameRepository.existsById(gameId)) {
            throw NoSuchElementException("Game with id '$gameId' not found")
        }
        return gameCopyRepository.findByGameIdAndIsAvailableTrue(gameId)
            .sortedBy { it.copyNumber }
            .map { copy ->
                GameCopyDto(
                    id = copy.id,
                    copyNumber = copy.copyNumber,
                    condition = copy.condition,
                    isAvailable = copy.isAvailable
                )
            }
    }
}

