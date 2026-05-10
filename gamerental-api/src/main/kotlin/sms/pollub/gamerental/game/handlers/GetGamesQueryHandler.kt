package sms.pollub.gamerental.game.handlers

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.queries.GameDto
import sms.pollub.gamerental.game.queries.GetGamesQuery
import sms.pollub.gamerental.game.repository.GameCopyRepository
import sms.pollub.gamerental.game.repository.GameRepository

@Component
class GetGamesQueryHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: GetGamesQuery): List<GameDto> {
        val games = if (query.tag != null) {
            gameRepository.findByTagsContaining(query.tag)
        } else {
            gameRepository.findAll()
        }

        val sorted = when (query.sortBy.lowercase()) {
            "date", "createdat" -> games.sortedBy { it.createdAt }
            else -> games.sortedBy { it.title.lowercase() }
        }

        return sorted.map { game ->
            val available = gameCopyRepository.countByGameIdAndIsAvailableTrue(game.id)
            GameDto(
                id = game.id,
                title = game.title,
                description = game.description,
                tags = game.tags.toSet(),
                totalCopies = game.totalCopies,
                availableCopies = available,
                createdAt = game.createdAt
            )
        }
    }
}

