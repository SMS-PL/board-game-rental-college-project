package sms.pollub.gamerental.game.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.queries.GameCopyDto
import sms.pollub.gamerental.game.queries.GameDetailDto
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.game.repository.GameRepository
import java.util.UUID

@Component
class GetGameQueryHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(gameId: UUID): GameDetailDto {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game with id '$gameId' not found") }

        val copies = gameCopyRepository.findByGameId(game.id)
            .sortedBy { it.copyNumber }
            .map { copy ->
                GameCopyDto(
                    id = copy.id,
                    copyNumber = copy.copyNumber,
                    condition = copy.condition,
                    isAvailable = copy.isAvailable
                )
            }

        return GameDetailDto(
            id = game.id,
            title = game.title,
            description = game.description,
            tags = game.tags.toSet(),
            totalCopies = game.totalCopies,
            copies = copies,
            createdAt = game.createdAt
        )
    }
}

